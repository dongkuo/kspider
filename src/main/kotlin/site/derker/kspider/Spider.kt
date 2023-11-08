package site.derker.kspider

import org.apache.logging.log4j.util.Supplier
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class Spider(vararg urls: String, parse: Doc.(Spider) -> Unit) {
    private val log = LoggerFactory.getLogger("gutenberg")
    private val httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofMillis(5000))
        .build()
    private val taskQueue: BlockingQueue<Task> = LinkedBlockingQueue()
    private val workers: List<Thread> = List(8) { i -> Thread(::loop, "spider-worker-" + (i + 1)) }
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var state: State = State.IDLE

    constructor(urlSupplier: Supplier<List<String>>, parse: Doc.(Spider) -> Unit) : this(
        *urlSupplier.get().toTypedArray(),
        parse = parse
    )

    init {
        addUrls(urls = urls, parse = parse)
    }

    fun addUrls(vararg urls: String, parse: (Doc, Spider) -> Unit) {
        urls.forEach {
            taskQueue.put(Task(it, parse))
        }
    }

    fun start() {
        if (state != State.TERMINAL) {
            synchronized {
                if (state != State.TERMINAL) {
                    state = State.RUNNING
                    condition.signal()
                }
            }
        }
        if (state == State.TERMINAL) {
            log.warn("the spider had been terminal")
        }
    }

    fun pause() {
        synchronized {
            state = State.IDLE
            condition.signal()
        }
    }

    fun stop() {
        synchronized {
            state = State.TERMINAL
            condition.signal()
        }
    }

    private fun loop() {
        try {
            while (state != State.TERMINAL) {
                // poll task queue
                while (state == State.RUNNING) {
                    val task = taskQueue.poll(2, TimeUnit.SECONDS)
                    if (task != null) {
                        run(task)
                    }
                }
                // idle
                synchronized {
                    if (state != State.TERMINAL) {
                        condition.await()
                    }
                }
            }
        } catch (e: InterruptedException) {
            Thread.interrupted()
        } catch (e: Exception) {
            log.error("worker occurs exception", e);
        }
    }

    private fun run(task: Task) {
        val req = HttpRequest.newBuilder()
            .uri(URI.create(task.url))
            .timeout(Duration.ofSeconds(5))
            .build()
        val resp = httpClient.send(req, BodyHandlers.ofString())
        val body = resp.body()
        if (resp.statusCode() == HttpURLConnection.HTTP_OK) {
            task.parse(Doc(body, this), this)
        } else {
            log.error("${task.url} request failed, statusCode=${resp.statusCode()}, body=${body}")
        }
    }

    private fun synchronized(block: () -> Unit) {
        try {
            lock.lock()
            block()
        } catch (e: InterruptedException) {
            Thread.interrupted()
        } finally {
            lock.unlock()
        }
    }

    enum class State {
        IDLE,
        RUNNING,
        TERMINAL
    }
}

data class Task(val url: String, val parse: (Doc, Spider) -> Unit)

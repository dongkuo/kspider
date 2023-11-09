package site.derker.kspider

import org.apache.logging.log4j.util.Supplier
import org.slf4j.LoggerFactory
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

/**
 * @param {@link Int} http response statusCode
 */
typealias DocHandler = Doc.(Int) -> Unit

enum class State {
    IDLE,
    RUNNING,
    TERMINAL
}

data class Task(val url: String, val docHandler: DocHandler)

class Spider(vararg urls: String, val spiderDocHandler: DocHandler) {
    val controller: Controller = Controller("spider")
    val taskQueue: BlockingQueue<Task> = LinkedBlockingQueue()
    private var fetchers = MutableList(8) {
        val thread = Thread(Fetcher(this), "fetcher-" + (it + 1))
        thread.start()
        return@MutableList thread
    }

    constructor(urlSupplier: Supplier<List<String>>, docHandler: DocHandler) : this(
        *urlSupplier.get().toTypedArray(),
        spiderDocHandler = docHandler
    )

    init {
        addUrls(urls = urls, docHandler = spiderDocHandler)
    }

    fun addUrls(vararg urls: String, docHandler: DocHandler = spiderDocHandler) {
        urls.forEach {
            taskQueue.put(Task(it, docHandler))
        }
    }

    fun start() {
        controller.start()
    }

    fun pause() {
        controller.pause()
    }

    fun stop() {
        controller.stop()
    }
}

class Controller(private val name: String) {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var state: State = State.IDLE

    companion object {
        private val log = LoggerFactory.getLogger(Companion::class.java)
    }

    fun start() {
        doIfNotTerminal("start") {
            state = State.RUNNING
            condition.signalAll()
        }
    }

    fun pause() {
        doIfNotTerminal("pause") {
            state = State.IDLE
            condition.signalAll()
        }
    }

    fun stop() {
        synchronized {
            state = State.TERMINAL
            condition.signalAll()
        }
    }

    private fun doIfNotTerminal(operation: String, block: () -> Unit) {
        val logWarning = {
            log.warn("$operation failed. $name had been terminal")
        }
        if (state == State.TERMINAL) {
            logWarning()
            return
        }
        synchronized {
            if (state == State.TERMINAL) {
                logWarning()
            } else {
                block()
            }
        }
    }

    fun loop(block: () -> Unit) {
        while (state != State.TERMINAL) {
            // run util state is not running
            while (state == State.RUNNING) {
                block()
            }
            // idle if necessary
            if (state == State.IDLE) {
                synchronized {
                    if (state == State.IDLE) {
                        try {
                            condition.await()
                        } catch (e: InterruptedException) {
                            Thread.interrupted()
                        }
                    }
                }
            }
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
}
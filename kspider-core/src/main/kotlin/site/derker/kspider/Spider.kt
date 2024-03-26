package site.derker.kspider

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

data class Options(
    val spiderName: String = "kspider",
    val fetcherNumber: Int = 16,
    val requestTimeoutMillis: Long? = 3000,
    val connectTimeoutMillis: Long? = 3000,
    val socketTimeoutMillis: Long? = 3000
)

enum class State {
    NEW,
    IDLE,
    RUNNING,
    TERMINAL
}

data class Task(val url: String, val handler: Handler<Response>)

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class SpiderDsl

typealias Handler<T> = suspend (@SpiderDsl T).() -> Unit
typealias ExtraHandler<T, E> = suspend (@SpiderDsl T).(E) -> Unit


class Spider(
    vararg startUrls: String,
    private val options: Options = Options(),
    private val defaultHandler: Handler<Response>
) : CoroutineScope {
    private val httpClient = HttpClient {
        followRedirects = true
        install(HttpTimeout)
    }
    private val state = AtomicReference(State.NEW)
    private val parentJob = Job()
    private val taskChannel: Channel<Task> = Channel(Channel.UNLIMITED)
    val log: Logger = LoggerFactory.getLogger(options.spiderName)

    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default

    init {
        // add start urls
        launch {
            addUrls(urls = startUrls, defaultHandler)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start(stopAfterFinishing: Boolean = true) {
        updateState(State.NEW, State.RUNNING) {
            // launch fetcher
            val fetchers = List(options.fetcherNumber) { Fetcher(this) }
            for (fetcher in fetchers) {
                launch {
                    fetcher.start()
                }
            }
            // wait all fetcher idle and task channel is empty
            runBlocking {
                var allIdleCount = 0
                while (true) {
                    val isAllIdle = fetchers.all { it.isIdle }
                    if (isAllIdle && taskChannel.isEmpty) {
                        allIdleCount++
                    } else {
                        allIdleCount = 0
                    }
                    if (allIdleCount == 2) {
                        fetchers.forEach { it.stop() }
                        return@runBlocking
                    }
                    delay(1000)
                }
            }
        }
    }

    fun pause() {
        updateState(State.RUNNING, State.IDLE)
    }

    fun resume() {
        updateState(State.IDLE, State.RUNNING)
    }

    fun stop() {
        updateState(State.IDLE, State.RUNNING, newState = State.TERMINAL) {
            parentJob.cancel()
        }
    }

    private fun updateState(exceptedState: State, newState: State, block: (() -> Unit)? = null): Boolean {
        val isUpdated = state.compareAndSet(exceptedState, newState)
        if (block != null && isUpdated) {
            block()
        }
        return isUpdated
    }

    private fun updateState(vararg exceptedStates: State, newState: State, block: (() -> Unit)? = null) {
        for (exceptedState in exceptedStates) {
            val isUpdated = updateState(exceptedState, newState, block)
            if (isUpdated) {
                return
            }
        }
    }

    suspend fun addUrls(vararg urls: String, handler: Handler<Response> = defaultHandler) {
        urls.forEach {
            log.debug("add url: $it")
            taskChannel.send(Task(it, handler))
        }
    }

    private class Fetcher(val spider: Spider) {
        var isIdle = true
            private set

        private var job: Job? = null

        suspend fun start() = withContext(spider.coroutineContext) {
            job = launch(CoroutineName("${spider.options.spiderName}-fetcher")) {
                while (true) {
                    isIdle = true
                    val task = spider.taskChannel.receive()
                    isIdle = false
                    spider.log.debug("fetch ${task.url}")
                    val httpStatement = spider.httpClient.prepareGet(task.url) {
                        timeout {
                            connectTimeoutMillis = spider.options.connectTimeoutMillis
                            requestTimeoutMillis = spider.options.requestTimeoutMillis
                            socketTimeoutMillis = spider.options.socketTimeoutMillis
                        }
                    }
                    httpStatement.execute {
                        val request = Request(URI.create(task.url).toURL(), "GET")
                        task.handler.invoke(Response(request, it, spider))
                    }
                }
            }
        }

        fun stop() {
            job?.cancel()
        }
    }
}




package site.derker.kspider

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.net.URL
import kotlin.coroutines.CoroutineContext

data class Options(
    val fetcherNumber: Int = 16,
    val requestTimeoutMillis: Long? = 3000,
    val connectTimeoutMillis: Long? = 3000,
    val socketTimeoutMillis: Long? = 3000
)

data class Task(val url: String, val handler: Handler<Response>)

class Spider(
    vararg startUrls: String,
    private val options: Options = Options(),
    private val globalHandler: Handler<Response>
) : CoroutineScope {

    private val httpClient = HttpClient {
        followRedirects = true
        install(HttpTimeout)
    }
    private val job = Job()
    private val taskChannel: Channel<Task> = Channel(Channel.UNLIMITED)
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        // add start urls
        launch {
            addUrls(urls = startUrls, globalHandler)
        }
        // launch fetcher
        repeat(options.fetcherNumber) {
            launch { fetch() }
        }
    }

    suspend fun addUrls(vararg urls: String, handler: Handler<Response> = globalHandler) {
        urls.forEach { taskChannel.send(Task(it, handler)) }
    }

    private suspend fun fetch() {
        val task = taskChannel.receive()
        val response = httpClient.get(task.url) {
            timeout {
                connectTimeoutMillis = options.connectTimeoutMillis
                requestTimeoutMillis = options.requestTimeoutMillis
                socketTimeoutMillis = options.socketTimeoutMillis
            }
        }
        val request = Request(URL(""), "GET")
        task.handler.invoke(Response(request, response))
    }
}


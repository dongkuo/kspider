package site.derker.kspider

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class KtorTests {

    val log = LoggerFactory.getLogger(KtorTests::class.java)
    val println: (String) -> Unit = log::info

    @Test
    fun get() {
        val client = HttpClient {
            followRedirects = true
            install(HttpTimeout)
        }
        runBlocking {
            val response: HttpResponse = client.get("http://baidu.com") {
                timeout {
                    requestTimeoutMillis = 400
                }
            }
            println(response.body<String>())
        }
        println("after runBlocking")
    }

    @Test
    fun test() {
        runBlocking {
            log.info("before")
            doWorld()
            log.info("after")
        }
    }

    private suspend fun doWorld() = coroutineScope {  // this: CoroutineScope
        launch {
            delay(2000L)
            println("World 2")
        }
        launch {
            delay(1000L)
            println("World 1")
        }
        println("Hello")
    }
}
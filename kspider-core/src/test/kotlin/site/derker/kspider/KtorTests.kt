package site.derker.kspider

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File

class KtorTests {

    val log = LoggerFactory.getLogger(KtorTests::class.java)
    val println: (String) -> Unit = log::info

    @Test
    fun get1() {
        val client = HttpClient {
            followRedirects = true
            install(HttpTimeout)
        }
        runBlocking {
            val response: HttpResponse = client.get("http://127.0.0.1") {
                timeout {
                    requestTimeoutMillis = 400
                }
            }
            println(response.body<String>())
        }
        println("after runBlocking")
    }

    @Test
    fun get2() {
        val client = HttpClient()
        runBlocking {
            val httpResponse: HttpResponse = client.get("https://ktor.io/")
            val stringBody: String = httpResponse.body<String>()
        }
    }
}
package site.derker.kspider

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
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
            val response: HttpResponse = client.get("https://www.cnblogs.com/dongkuo")
            launch(Dispatchers.Default) {
                println(response.body<String>())
                println("==============body===================")
            }
            launch(Dispatchers.Default) {
                println(response.bodyAsChannel().toInputStream())
                println("==============toInputStream===================")
            }
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
package site.derker.kspider

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.coroutineScope
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.nio.charset.Charset

typealias Handler<T> = T.() -> Unit
typealias HandlerExtra<T, E> = T.(E) -> Unit

class Request(val url: URL, var method: String) {

}

class HttpResponseException(message: String?) : RuntimeException(message)

class Response(
    val request: Request,
    private val response: HttpResponse,
) {
    fun html(handler: Handler<Document>) {
        val value = response.status.value
    }

    fun text(handler: Handler<String>?) {
    }

    inline fun <reified T> htmlExtract(handlerWithData: HandlerExtra<Document, T>): T {
        val data = T::class.constructors.first().call()
        return data
    }

    fun stream(handler: Handler<InputStream>?) {
    }

    fun header(name: String): String? {
        return response.headers[name]
    }

    fun statusCode(): Int {
        return response.status.value
    }
}

package site.derker.kspider

import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.InputStream
import java.net.URL
import kotlin.reflect.full.createInstance

typealias Handler<T> = suspend T.() -> Unit
typealias HandlerExtra<T, E> = suspend T.(E) -> Unit

class Request(val url: URL, var method: String)

class HttpResponseException(message: String?) : RuntimeException(message)

class Response(
    val request: Request,
    private val response: HttpResponse,
    private val spider: Spider
) {

    fun statusCode(): Int = response.status.value

    fun header(name: String): String? {
        return response.headers[name]
    }

    suspend fun text(): String = response.bodyAsText()

    suspend fun html(): Document = Document(text(), response.request.url.toString(), spider)

    suspend fun html(handler: Handler<Document>) {
        handler.invoke(html())
    }

    suspend inline fun <reified T: Any> htmlExtract(crossinline handler: HandlerExtra<Document, T>): T {
        val data = T::class.createInstance()
        html { handler.invoke(this, data) }
        return data
    }

    suspend fun stream() = response.bodyAsChannel().toInputStream()

    suspend fun stream(handler: Handler<InputStream>) {
        handler.invoke(stream())
    }
}

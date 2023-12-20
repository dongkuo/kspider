package site.derker.kspider

import Handler
import ExtraHandler
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import kotlin.io.path.Path
import kotlin.io.use
import kotlin.reflect.full.createInstance


data class Progression(val receivedBytes: Long, val totalBytes: Long)
class Request(val url: URL, var method: String)

class Response(
    val request: Request,
    private val response: HttpResponse,
    private val spider: Spider
) {
    private var cachedTextBody: String? = null

    fun statusCode(): Int = response.status.value

    fun header(name: String): String? {
        return response.headers[name]
    }

    suspend fun text(): String {
        if (cachedTextBody == null) {
            cachedTextBody = response.bodyAsText()
        }
        return cachedTextBody!!
    }

    suspend fun html(): Document = Document(text(), response.request.url.toString(), spider)

    suspend fun html(handler: Handler<Document>) {
        handler.invoke(html())
    }

    suspend inline fun <reified T : Any> htmlExtract(crossinline handler: ExtraHandler<Document, T>): T {
        val data = T::class.createInstance()
        html { handler.invoke(this, data) }
        return data
    }

    suspend fun stream() = response.bodyAsChannel().toInputStream()

    suspend fun stream(handler: Handler<InputStream>) {
        stream().use { handler.invoke(it) }
    }

    suspend fun download(
        filePath: String? = null,
        direction: String? = null,
        onDownload: Handler<Progression>? = null
    ) {
        var finalFilePath = filePath ?: (resolveFilePath(direction) + parseFileName(response))
        finalFilePath = fixInvalidFilePathIfNecessary(finalFilePath)
        createParentDirectionIfNecessary(finalFilePath)
        spider.log.debug("download to $finalFilePath")

        var receivedBytes: Long = 0
        val totalBytes = response.headers["Content-Length"]?.toLong() ?: -1

        coroutineScope {
            if (cachedTextBody == null) {
                val channel: ByteReadChannel = response.bodyAsChannel()
                FileOutputStream(finalFilePath).use {
                    while (!channel.isClosedForRead) {
                        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                        while (!packet.isEmpty) {
                            val bytes = packet.readBytes()
                            it.write(bytes)
                            receivedBytes += bytes.size
                            onDownload?.invoke(Progression(receivedBytes, totalBytes))
                        }
                    }
                }
            } else {
                FileOutputStream(finalFilePath).use {
                    val byteArray = cachedTextBody!!.toByteArray()
                    val size = byteArray.size.toLong()
                    it.write(byteArray)
                    onDownload?.invoke(Progression(size, size))
                }
            }
        }
    }

    private val invalidFileNamePattern = Regex("[<>:\"|?*]")
    private fun fixInvalidFilePathIfNecessary(filePath: String): String {
        return filePath.replace(invalidFileNamePattern, "_")
    }

    private fun createParentDirectionIfNecessary(filePath: String) {
        val parentFile = Path(filePath).parent.toFile()
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
    }

    private fun resolveFilePath(direction: String?): String =
        if (direction == null) "./"
        else if (!direction.endsWith("/") && !direction.endsWith("\\")) direction + File.separator
        else direction

    private fun parseFileName(response: HttpResponse): String {
        val fileName = response.headers["Content-Disposition"]?.let {
            ContentDisposition.parse(it).parameter("filename")
        }
        if (fileName != null) {
            return fileName
        }
        val path = response.request.url.fullPath
        if (path == "" || path == "/") {
            return System.currentTimeMillis().toString() + ".download"
        }
        return path.substring(path.lastIndexOf('/') + 1)
    }
}

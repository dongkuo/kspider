package site.derker.kspider

import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.TimeUnit

class Fetcher(private val spider: Spider) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(Companion::class.java)
        private val httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofMillis(5000))
            .build()
    }

    override fun run() {
        spider.controller.loop {
            try {
                val task = spider.taskQueue.poll(2, TimeUnit.SECONDS)
                if (task != null) {
                    fetch(task)
                }
            } catch (e: InterruptedException) {
                Thread.interrupted()
            } catch (e: Exception) {
                log.error("fetcher occurs exception", e)
            }
        }
    }

    private fun fetch(task: Task) {
        val req = HttpRequest.newBuilder()
            .uri(URI.create(task.url))
            .timeout(Duration.ofSeconds(5))
            .build()
        val resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString())
        val body = resp.body()
        val statusCode = resp.statusCode()
        if (statusCode in 400..500) {
            log.warn("${task.url} response status code is $statusCode")
        }
        task.docHandler(Doc(body, spider), statusCode)
    }
}
package site.derker.kspider

import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class SpiderTest {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(Companion::class.java)
    }

    @Test
    fun test1() {
        val spider = Spider("https://www.cnblogs.com/dongkuo") {
            // follow the article link
            followAll(".forFlow .day .postTitle2") {
                parseArticle(this)
            }
            // follow next page
            follow("#nav_next_page a")
            follow("#homepage_bottom_pager a:containsOwn(下一页)")
        }
        spider.start()
        TimeUnit.SECONDS.sleep(10)
        spider.stop()
    }

    private fun parseArticle(doc: Doc) {
        val title = doc.select("#cb_post_title_url span")?.text()
        log.info("$title")
    }
}
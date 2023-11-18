package site.derker.kspider

import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SpiderTests {

    private val log: Logger = LoggerFactory.getLogger(SpiderTests::class.java)

    fun test1() {
        val spider = Spider("https://example.site") { // this: Response
            html { // this: Document : Selector
                // extract: Extractor
                var element1 = css(".class") // Element: Extractor
                var elements1 = cssAll(".class")

                var element2 = xpath("//")
                var elements2 = xpathAll("//")

                follow(".class") {
                    var data: MyData = htmlExtract<MyData> { // this: Response, it: MyData
                        it.url = this@follow.request.url.toString()
                        it.statusCode = this@follow.statusCode()
                        it.title = css("#title")?.text()
                    }
                }

                follow(xpath = ".class") { // this: Document
                    // for downloading file
                    stream {

                    }
                }
            }
        }
        spider.start()
//        spider.pause()
//        spider.resume()
//        spider.stop()
    }

    @Test
    fun test2() {
        val spider = Spider("https://www.cnblogs.com/dongkuo") {
            html {
                // 文章详情页
                follow(".postTitle2") {
                    val article = htmlExtract<Article> {
                        it.url = this@follow.request.url.toString()
                        it.title = css("#cb_post_title_url")?.text()
                    }
                    log.info("$article")
                }
                // 下一页
                follow("#nav_next_page a")
                follow("#homepage_bottom_pager a:containsOwn(下一页)")
            }
        }
        spider.start()
    }
}

data class MyData(var url: String, var statusCode: Int, var title: String?)
data class Article(var url: String? = null, var title: String? = null)
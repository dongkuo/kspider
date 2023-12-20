package site.derker.kspider

import org.junit.jupiter.api.Test

class SpiderTests {

    @Test
    fun test2() {
        val spider = Spider("https://www.cnblogs.com/dongkuo") {
            html {
                // 文章详情页
                follow(".postTitle2:eq(0)") {
                    val article = htmlExtract<Article> {
                        it.url = this@follow.request.url.toString()
                        it.title = css("#cb_post_title_url")?.text() ?: title()
                    }
                    // 下载
                    download("./blogs/${article.title}.html")
                }
                // 下一页
                follow("#nav_next_page a")
                follow("#homepage_bottom_pager a:containsOwn(下一页)")
            }
        }
        spider.start()
    }
}

data class Article(var url: String? = null, var title: String? = null)
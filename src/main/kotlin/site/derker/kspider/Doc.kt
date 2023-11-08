package site.derker.kspider

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Doc(html: String, private val spider: Spider) {

    private var jsoupDoc: Document = Jsoup.parse(html)

    fun queryByCss(selector: String, handler: Ele?.() -> Unit) {
        val jsoupEle = jsoupDoc.selectFirst(selector)
    }

    fun queryAllByCss(selector: String, handler: List<Ele>?.() -> Unit) {
    }

    fun addUrls(vararg urls: String, parse: Doc.(Spider) -> Unit) {
        spider.addUrls(urls = urls, parse = parse)
    }

    companion object {
        fun parse(html: String): Doc {
            val jDoc = Jsoup.parse(html)

        }
    }

}

class Ele(val ele: Element, private val spider: Spider) {

    fun addUrls(vararg urls: String, parse: Doc?.(Spider) -> Unit) {
        spider.addUrls(urls = urls, parse = parse)
    }
}

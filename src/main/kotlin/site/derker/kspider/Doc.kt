package site.derker.kspider

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Doc(html: String, private val spider: Spider) {

    private var jsoupDoc: Document = Jsoup.parse(html)

    fun queryByCss(selector: String, handler: Ele?.() -> Unit) {
        val jsoupEle = jsoupDoc.selectFirst(selector)
        if (jsoupEle == null) {
            handler(jsoupEle)
        } else {
            handler(Ele(jsoupEle, spider))
        }
    }

    fun queryAllByCss(selector: String, handler: List<Ele>.(Spider) -> Unit) {
        val jsoupEles = jsoupDoc.select(selector)
        if (jsoupEles.isEmpty()) {
            handler(listOf(), spider)
        } else {
            val eleList = jsoupEles.map { Ele(it, spider) }.toList()
            handler(eleList, spider)
        }
    }

    fun addUrls(vararg urls: String, parse: Doc.(Spider) -> Unit) {
        spider.addUrls(urls = urls, parse = parse)
    }
}

class Ele(val jsoupEle: Element, private val spider: Spider) {

    fun addUrls(vararg urls: String, parse: Doc?.(Spider) -> Unit) {
        spider.addUrls(urls = urls, parse = parse)
    }
}

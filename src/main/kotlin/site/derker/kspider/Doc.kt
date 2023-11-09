package site.derker.kspider

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Doc(html: String, private val spider: Spider) {

    private var jsoupDoc: Document = Jsoup.parse(html)

    fun select(selector: String, handler: (Ele?.() -> Unit)? = null): Ele? {
        val jsoupEle = jsoupDoc.selectFirst(selector)
        val ele = if (jsoupEle == null) null else Ele(jsoupEle, spider)
        if (handler != null) {
            handler(ele)
        }
        return ele
    }

    fun selectAll(selector: String, handler: (List<Ele>.() -> Unit)? = null): List<Ele> {
        val jsoupEles = jsoupDoc.select(selector)
        val eleList = if (jsoupEles.isEmpty()) listOf() else jsoupEles.map { Ele(it, spider) }.toList()
        if (handler != null) {
            handler(eleList)
        }
        return eleList
    }

    fun follow(selector: String, attrName: String = "href", docHandler: DocHandler = spider.spiderDocHandler) {
        select(selector) { this?.follow(attrName, docHandler) }
    }

    fun followAll(selector: String, attrName: String = "href", docHandler: DocHandler = spider.spiderDocHandler) {
        selectAll(selector) {
            forEach { it.follow(attrName, docHandler) }
        }
    }
}

class Ele(private val jsoupEle: Element, private val spider: Spider) {

    fun follow(attrName: String = "abs:href", docHandler: DocHandler = spider.spiderDocHandler) {
        val url = this.attr(attrName)
        spider.addUrls(url, docHandler = docHandler)

    }

    fun ownText(): String {
        return jsoupEle.ownText()
    }

    fun text(): String {
        return jsoupEle.text()
    }

    fun attr(name: String): String {
        return jsoupEle.attr(name)
    }

    fun hrefAttr(absolute: Boolean = true): String {
        val attrName = if (absolute) "abs:href" else "href"
        return jsoupEle.attr(attrName)
    }

    override fun toString(): String {
        return jsoupEle.toString()
    }
}

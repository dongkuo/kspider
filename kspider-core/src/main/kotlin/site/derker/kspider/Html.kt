package site.derker.kspider

import org.jsoup.Jsoup

typealias InnerDocument = org.jsoup.nodes.Document
typealias InnerElement = org.jsoup.nodes.Element

class Document(
    html: String,
    baseUrl: String,
    private val spider: Spider
) : Selector {

    private val innerDocument: InnerDocument

    init {
        innerDocument = Jsoup.parse(html, baseUrl)
    }

    suspend fun follow(
        css: String? = null,
        xpath: String? = null,
        extract: Extract = attribute("href"),
        handler: Handler<Response>? = null
    ) {
        if (css != null) {
            follow(cssAll(css), extract, handler)
        }
        if (xpath != null) {
            follow(xpathAll(xpath), extract, handler)
        }
    }

    suspend fun follow(
        element: Element?,
        extract: Extract = attribute("href"),
        handler: Handler<Response>? = null
    ) {
        val url = element.let(extract) ?: return
        if (handler == null) {
            spider.addUrls(url)
        } else {
            spider.addUrls(url, handler = handler)
        }
    }

    suspend fun follow(
        elements: List<Element>,
        extract: Extract = attribute("href"),
        responseHandler: Handler<Response>? = null
    ) {
        elements.forEach { follow(it, extract, responseHandler) }
    }

    override fun css(selector: String): Element? {
        return innerDocument.selectFirst(selector)?.let { Element(it) }
    }

    override fun cssAll(selector: String): List<Element> {
        return innerDocument.select(selector).map { Element((it)) }.toList()

    }

    override fun xpath(selector: String): Element? {
        return innerDocument.selectXpath(selector).map { Element((it)) }.firstOrNull()
    }

    override fun xpathAll(selector: String): List<Element> {
        return innerDocument.selectXpath(selector).map { Element((it)) }.toList()

    }

    override fun firstChild(): Element? {
        return innerDocument.firstElementChild()?.let { Element(it) }
    }

    override fun lastChild(): Element? {
        return innerDocument.lastElementChild()?.let { Element(it) }
    }

    override fun nthChild(index: Int): Element? {
        if (index < 0 || index >= innerDocument.childrenSize()) {
            return null
        }
        return Element(innerDocument.child(index))
    }

    override fun children(): List<Element> {
        return innerDocument.children().map { Element((it)) }.toList()
    }
}

class Element(private val innerElement: InnerElement) : Selector, Extractor {
    override fun css(selector: String): Element? {
        return innerElement.selectFirst(selector)?.let { Element(it) }
    }

    override fun cssAll(selector: String): List<Element> {
        return innerElement.select(selector).map { Element((it)) }.toList()

    }

    override fun xpath(selector: String): Element? {
        return innerElement.selectXpath(selector).map { Element((it)) }.firstOrNull()
    }

    override fun xpathAll(selector: String): List<Element> {
        return innerElement.selectXpath(selector).map { Element((it)) }.toList()

    }

    override fun firstChild(): Element? {
        return innerElement.firstElementChild()?.let { Element(it) }
    }

    override fun lastChild(): Element? {
        return innerElement.lastElementChild()?.let { Element(it) }
    }

    override fun nthChild(index: Int): Element? {
        if (index < 0 || index >= innerElement.childrenSize()) {
            return null
        }
        return Element(innerElement.child(index))
    }

    override fun children(): List<Element> {
        return innerElement.children().map { Element((it)) }.toList()
    }

    override fun tag(): String? {
        return innerElement.tag().normalName()
    }

    override fun html(onlyInner: Boolean): String? {
        return if (onlyInner) innerElement.html() else innerElement.outerHtml()
    }

    override fun text(onlyOwn: Boolean): String? {
        return if (onlyOwn) innerElement.ownText() else innerElement.text()
    }

    override fun attribute(name: String, absoluteUrl: Boolean): String {
        var newName = name
        if (absoluteUrl && !name.startsWith("abs:")) {
            newName = "abs:$name"
        }
        return innerElement.attr(newName)
    }
}

interface Selector {
    fun css(selector: String): Element?
    fun cssAll(selector: String): List<Element>
    fun xpath(selector: String): Element?
    fun xpathAll(selector: String): List<Element>
    fun firstChild(): Element?
    fun lastChild(): Element?
    fun nthChild(index: Int): Element?
    fun children(): List<Element>
}

interface Extractor {
    fun tag(): String?
    fun html(onlyInner: Boolean = false): String?
    fun text(onlyOwn: Boolean = false): String?
    fun attribute(name: String, absoluteUrl: Boolean = true): String
}


typealias Extract = (Extractor?) -> String?

fun tag(): Extract = { it?.tag() }
fun html(): Extract = { it?.html() }
fun attribute(name: String): Extract = { it?.attribute(name) }
fun text(): Extract = { it?.text() }
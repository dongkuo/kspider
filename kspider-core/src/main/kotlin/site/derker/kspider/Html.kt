package site.derker.kspider

import org.jsoup.Jsoup

typealias InnerDocument = org.jsoup.nodes.Document
typealias InnerElement = org.jsoup.nodes.Element

class Document(
    html: String,
    baseUrl: String,
    private val spider: Spider
) : Selectable {

    private val innerDocument: InnerDocument

    init {
        innerDocument = Jsoup.parse(html, baseUrl)
    }

    suspend fun follow(
        css: String? = null,
        xpath: String? = null,
        extractor: Extractor = attribute("href"),
        handler: Handler<Response>? = null
    ) {
        if (css != null) {
            follow(cssAll(css), extractor, handler)
        }
        if (xpath != null) {
            follow(xpathAll(xpath), extractor, handler)
        }
    }

    suspend fun follow(
        extractableList: List<Extractable>,
        extractor: Extractor = attribute("href"),
        responseHandler: Handler<Response>? = null
    ) {
        extractableList.forEach { follow(it, extractor, responseHandler) }
    }

    suspend fun follow(
        extractable: Extractable?,
        extractor: Extractor = attribute("href"),
        handler: Handler<Response>? = null
    ) {
        val url = extractable.let(extractor) ?: return
        spider.addUrls(url, handler = handler)
    }

    fun title(): String {
        return innerDocument.title()
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

class Element(private val innerElement: InnerElement) : Selectable, Extractable {
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

interface Selectable {
    fun css(selector: String): Element?
    fun cssAll(selector: String): List<Element>
    fun xpath(selector: String): Element?
    fun xpathAll(selector: String): List<Element>
    fun firstChild(): Element?
    fun lastChild(): Element?
    fun nthChild(index: Int): Element?
    fun children(): List<Element>
}

interface Extractable {
    fun tag(): String?
    fun html(onlyInner: Boolean = false): String?
    fun text(onlyOwn: Boolean = false): String?
    fun attribute(name: String, absoluteUrl: Boolean = true): String
}


typealias Extractor = (Extractable?) -> String?

fun tag(): Extractor = { it?.tag() }
fun html(): Extractor = { it?.html() }
fun attribute(name: String): Extractor = { it?.attribute(name) }
fun text(): Extractor = { it?.text() }
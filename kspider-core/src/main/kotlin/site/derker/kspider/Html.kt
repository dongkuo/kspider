package site.derker.kspider

class Document(val spider: Spider) : Selector {

    fun follow(
        css: String? = null,
        xpath: String? = null,
        extract: Extract = attribute("abs:href"),
        handler: Handler<Response>? = null
    ): ResponseFuture {
        if (css != null) {
            return follow(css(css), extract, handler)
        }
        if (xpath != null) {
            return follow(xpath(xpath), extract, handler)
        }
        TODO()
    }

    fun follow(
        element: Element?,
        extract: Extract = attribute("abs:href"),
        handler: Handler<Response>? = null
    ): ResponseFuture {
        val responseFuture = ResponseFuture()
        val url = element.let(extract) ?: return responseFuture
        spider.addUrls(url, handler = object : (Response) -> Unit {
            override fun invoke(response: Response) {
                responseFuture.callback(response)
            }
        })
        return responseFuture
    }

    fun follow(
        elements: List<Element>,
        extract: Extract = attribute("href"),
        responseHandler: Handler<Response>? = null
    ) {
        elements.forEach { follow(it, extract, responseHandler) }
    }
}

class Element : Selector, Extractor {

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
    fun attribute(name: String): String?
}


typealias Extract = (Extractor?) -> String?

fun tag(): Extract = { it?.tag() }
fun html(): Extract = { it?.html() }
fun attribute(name: String): Extract = { it?.attribute(name) }
fun text(): Extract = { it?.text() }
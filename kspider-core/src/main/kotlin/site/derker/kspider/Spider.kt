package site.derker.kspider

data class Options(val fetcherNumber: Int = 16)

class Spider(
    vararg startUrls: String,
    private val options: Options = Options(),
    private val globalHandler: Handler<Response>
) {

    fun addUrls(vararg urls: String, handler: Handler<Response>? = globalHandler) {
        urls.forEach {
            TODO()
        }
    }
}


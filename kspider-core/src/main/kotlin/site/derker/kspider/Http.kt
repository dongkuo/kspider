package site.derker.kspider

import java.io.InputStream

typealias Handler<T> = T.() -> Unit
typealias HandlerWithData<T, D> = T.(D) -> Unit

class Request {

}

class Response(val spider: Spider) {
    fun html(handler: Handler<Document>?) {
        // parse response to document
    }

    fun <D> htmlExtract(handlerWithData: HandlerWithData<Document, D>): D {
        TODO()
    }

    fun stream(handler: Handler<InputStream>?) {
        // parse response to document
    }

}

class ResponseFuture {
    private var handler: Handler<*>? = null
    private var response: Response? = null

    fun html(handler: Handler<Document>?) {
        if (handler == null) {
            return
        }
        if (response == null) {
            this.handler = handler
        } else {
            this.response!!.html(handler)
        }
    }

    fun stream(handler: Handler<InputStream>?) {
        if (handler == null) {
            return
        }
        if (response == null) {
            this.handler = handler
        } else {
            this.response!!.stream(handler)
        }
    }

    inline fun <reified D> htmlExtract(handler: HandlerWithData<Document, D>): D {
        with(D::class) {
            if (response == null) {
                this.handler = handler
            } else {
                this.response!!.stream(handler)
            }
        }
    }

    fun callback(response: Response) {
        TODO("Not yet implemented")
    }
}

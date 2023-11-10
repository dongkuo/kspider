package site.derker.kspider

import java.io.InputStream

typealias Handler<T, R> = T.(R) -> Unit

class Request {

}

class Response(val spider: Spider) {
    fun html(handler: Handler<Document, Unit>?) {
        // parse response to document
    }

    fun <D> htmlExtract(handlerWithData: Handler<Document, D>): D {
        TODO()
    }

    fun stream(handler: Handler<InputStream, Unit>?) {
        // parse response to document
    }

}

class ResponseFuture {
    var handler: Handler<*>? = null
    var response: Response? = null

    fun html(handler: Handler<Document, Unit>?) {
        if (handler == null) {
            return
        }
        if (response == null) {
            this.handler = handler
        } else {
            this.response!!.html(handler)
        }
    }

    fun stream(handler: Handler<InputStream, Unit>?) {
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
        if (response == null) {
            this.handler = handler
        }
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

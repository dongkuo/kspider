package site.derker.kspider

class SpiderTests {

    fun test1() {
        val spider = Spider("https://example.site") { // this: Response
            html { // this: Document : Selector
                // extract: Extractor
                var element1 = css(".class") // Element: Extractor
                var elements1 = cssAll(".class")

                var element2 = xpath("//")
                var elements2 = xpathAll("//")

                follow(".class") {
                    var data: MyData = htmlExtract<MyData> { // this: Response, it: MyData
                        it.url = this@follow.request.uri.toString()
                        it.statusCode = this@follow.statusCode()
                        it.title = css("#title")?.text()
                    }
                }

                follow(xpath = ".class") { // this: Document
                    // for downloading file
                    stream {

                    }
                }
            }
        }
//        spider.start(stopAfterFinishing = true)
//        spider.pause()
//        spider.resume()
//        spider.stop()
    }
}

data class MyData(var url: String, var statusCode: Int, var title: String?)
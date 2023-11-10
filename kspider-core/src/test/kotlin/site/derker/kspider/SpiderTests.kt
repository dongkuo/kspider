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

                follow(".class") { // this: Response
                    html {

                    }

                    stream {

                    }

                    val data = htmlExtract<MyData> {

                    }
                }

                follow(xpath = ".class").html { // this: Document
                }

                // for downloading file
                follow(css(".class")).stream { // this: InputStream

                }

                // for extracting data
                var data: MyData = follow("#id").htmlExtract<MyData> { // this: Response, it: MyData
                    it.url = request.url
                    it.statusCode = statusCode
                    html {
                        it.title = css("#title")?.text()
                    }
                }
            }
        }
        spider.start(stopAfterFinishing = true)
        spider.pause()
        spider.resume()
        spider.stop()
    }
}

data class MyData(var url: String, var statusCode: Int, var title: String?)
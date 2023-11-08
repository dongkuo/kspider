package site.derker.kspider

import org.junit.jupiter.api.Test

class SpiderTest {

    @Test
    fun test1() {
        Spider("url1", "url2") {
            queryByCss("css selector") {
                addUrls("url1", "url2") {

                }
            }

            queryAllByCss("css selector") {

            }
        }
    }
}
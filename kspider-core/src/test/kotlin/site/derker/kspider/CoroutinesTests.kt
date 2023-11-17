package site.derker.kspider

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.seconds

class CoroutinesTests {

    private val log: Logger = LoggerFactory.getLogger("CoroutinesTeests")

    @Test
    fun testDeferred() = runBlocking {
        val deferreds: List<Deferred<Int>> = (1..3).map {
            async {
                loadData(it)
            }
        }
        val sum = deferreds.awaitAll().sum()
        println("$sum")
    }

    @Test
    fun test2() {
        runBlocking {
            log.info("runBlocking")
            launch {
                log.info("launch")
                yield()
            }
        }
    }

    @Test
    fun test4() = runBlocking<Unit> {
        // Launch a concurrent coroutine to check if the main thread is blocked
        launch {
            for (k in 1..3) {
                log.info("I'm not blocked $k")
                delay(100)
            }
        }
        // Collect the flow
        simple().collect { value -> log.info("$value") }
//        listOf(1, 2, 3).forEach {
//            TimeUnit.MILLISECONDS.sleep(100)
//            log.info("$it")
//        }
    }

    private fun simple(): Flow<Int> = flow { // flow builder
        for (i in 1..3) {
            delay(100) // pretend we are doing something useful here
            emit(i) // emit next value
        }
    }

    @Test
    fun testDelayInLaunch() = runTest {
        val realStartTime = System.currentTimeMillis()
        val virtualStartTime = currentTime

        foo()

        println("${System.currentTimeMillis() - realStartTime} ms") // ~ 11 ms
        println("${currentTime - virtualStartTime} ms")             // 1000 ms
    }

    private suspend fun foo() {
        delay(1000)    // auto-advances without delay
        println("foo") // executes eagerly when foo() is called
    }

    @Test
    fun test3() {
        runBlocking {
            try {
                concurrentSum()
            } catch (e: Exception) {
                log.error("", e)
            }
        }
    }

    private suspend fun concurrentSum() = coroutineScope {
        val time = measureTimeMillis {
            val one = async { doSomethingUsefulOne() }
            val two = async { doSomethingUsefulTwo() }
            one.await() + two.await()
        }
        log.info("time: $time")
    }

    private suspend fun doSomethingUsefulOne(): Int {
        log.info("before doSomethingUsefulOne")
        delay(1300L) // pretend we are doing something useful here
        log.info("after doSomethingUsefulOne")
        return 13
    }

    private suspend fun doSomethingUsefulTwo(): Int {
        delay(1000L) // pretend we are doing something useful here, too
        return 29
    }

    private suspend fun loadData(i: Int): Int {
        println("loading...")
        delay(1000L)
        println("loaded!")
        return i
    }


    @Test
    fun test5() = runBlocking {
        val numbers = produceNumbers() // produces integers from 1 and on
        val squares = square(numbers) // squares integers
        repeat(5) {
            println(squares.receive()) // print first five
        }
        println("Done!") // we are done
        coroutineContext.cancelChildren() // cancel children coroutines
    }

    fun CoroutineScope.produceNumbers() = produce {
        var x = 1
        while (true) send(x++) // infinite stream of integers starting from 1
    }

    fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
        for (x in numbers) send(x * x)
    }
}

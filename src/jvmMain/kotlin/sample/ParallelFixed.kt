package sample

import kotlinx.coroutines.*
import java.util.concurrent.Executors


class Main {
    private val es = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    fun process() = runBlocking {
        delay(2000)
        println("FIRST")
        while (true) {
            println("LOOP")
            async {
                println("INNER THING")
            }
            async {
                delay(500)
                println("INNER THING")
            }
            val blockResult = execute()

            println("Block result $blockResult")
        }
    }

    private suspend fun execute() = withContext(es) {

        Thread.sleep(1000) // simulating kafka consumer.poll(... )
        1
    }

    suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
        map { async { f(it) } }.awaitAll()
    }

    suspend fun <A, B> Iterable<A>.pmap2(f: suspend (A) -> B): List<B> = withContext(es) {
        map { async { f(it) } }.awaitAll()
    }

    suspend fun process2() {
        (0..9).pmap2 {
            sharade(it)
        }
    }

    private fun sharade(it: Int) {
        println("${it} INIT")
        Thread.sleep(300)
        println("${it} CONT")
        Thread.sleep(300)
        println("${it} DONE")
    }

    suspend fun process3() {
        (0..9).forEach {
            withContext(es) {
                sharade(it)
            }
        }
    }
}


suspend fun main() {
    Main().process2()
}
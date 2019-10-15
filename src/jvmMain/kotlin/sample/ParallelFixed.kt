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
}


fun main() {
    Main().process()
}
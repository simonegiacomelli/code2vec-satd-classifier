package coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

// This is an example of a three-stage multithreaded processing pipeline in Kotlin,
// with blocking operations occurring in all three stages of the pipeline, implemented
// using Kotlin `Channel` and coroutine objects.
//
// source -> filter -> output
//
// Thread.sleep is used to simulate a blocking IO operation.

suspend fun source(cout: Channel<Int>) {
    println("Source starting")
    for (i in 1..10) {
        val x = (0..255).random()
        cout.send(x)
        println("Source iteration $i sent $x")
        withContext(Dispatchers.IO) {
            val sleep: Long = (400..600).random().toLong()
            Thread.sleep(sleep)
        }
    }
    cout.close()
    println("Source exiting")
}

suspend fun filter(cin: Channel<Int>, cout: Channel<String>) {
    println("  Filter starting")
    for (x in cin) {
        println("  Filter received $x")
        withContext(Dispatchers.IO) {
            val sleep: Long = (400..600).random().toLong()
            Thread.sleep(sleep)
        }
        val y = "'$x'"
        cout.send(y)
        println("  Filter sent $y")
    }
    cout.close()
    println("  Filter exiting")
}

suspend fun output(cin: Channel<String>) {
    println("    Output starting")
    for (x in cin) {
        println("    Output received $x")
        withContext(Dispatchers.IO) {
            val sleep: Long = (400..600).random().toLong()
            Thread.sleep(sleep)
        }
    }
    println("    Output exiting")
}

const val queueSize = 2

fun runAll() {
    runBlocking {
        println("runAll starting")
        val pipe1 = Channel<Int>(queueSize)
        val pipe2 = Channel<String>(queueSize)
        GlobalScope.launch {
            launch { source(pipe1) }
            launch { filter(pipe1, pipe2) }
            launch { output(pipe2) }
        }.join()
    }
    println("runAll exiting")
}

fun main() {
    println("main starting")
    runAll()
    println("main exiting")
}
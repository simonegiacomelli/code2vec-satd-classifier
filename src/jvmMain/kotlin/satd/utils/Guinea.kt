package satd.utils

import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ForkJoinPool


fun main() {
    commonPoolInfo()
    exceptionToString()
}

private fun exceptionToString() {
    try {
        val xx = "ciao"
        println(xx.get(20))
    } catch (ex: Throwable) {
        println(StringWriter().also { ex.printStackTrace(PrintWriter(it)) }.toString())
    }
}

private fun commonPoolInfo() {
    println("ForkJoinPool.commonPool() ${ForkJoinPool.commonPool()} ")
}
package satd.utils

import satd.utils.RepoList.Companion.androidReposFull
import satd.utils.RepoList.Companion.androidReposFull2
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ForkJoinPool


fun main() {
    RepoList.androidReposFull2
        .sorted()
        .joinToString("\n")
        .also {
            Folders.data.resolve("androidReposFull2.txt").toFile().writeText(it)
        }
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
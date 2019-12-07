package satd.utils

import satd.utils.RepoList.Companion.androidReposFull
import satd.utils.RepoList.Companion.androidReposFull2
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ForkJoinPool


fun main() {
    val s1 = androidReposFull.toSet()
    val s2 = androidReposFull2.toSet()

    val s1_intersection_s2 = s1.intersect(s2)
    println("${s1.size} ${s2.size} ${s1_intersection_s2.size}")
    s1.subtract(s2).forEach {
        println(it)
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
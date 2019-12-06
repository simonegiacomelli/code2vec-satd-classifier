package satd.utils

import org.joda.time.DateTime
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ForkJoinPool

val baseUrl =
    "https://api.github.com/search/repositories?q=language:Java+topic:android+is:public+created:2019-01-01..2019-01-31&page=1&per_page=100"

fun main() {
    fun dateTimeToStr(): String = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss")
    )

    val g = Folders.database.resolve("github").toFile()
    g.mkdirs()
    var year = 2019;
    while (true) {
        if (year < 2008)
            break
        val start = DateTime(year, 1, 1, 0, 0)
        val end = DateTime(year, 12, 31, 0, 0)

        val dts = start.toString("yyyy-MM-dd")
        val dte = end.toString("yyyy-MM-dd")
        val url =
            "https://api.github.com/search/repositories?q=language:Java+topic:android+is:public+created:$dts..$dte&page=1&per_page=100"
        println(url)
        val content = URL(url).readText()
        g.resolve("$year.json").writeText(
            "$url\n$content"
        )
        year--

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
package satd.utils

import com.google.gson.JsonParser
import org.joda.time.DateTime
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL
import java.util.concurrent.ForkJoinPool


fun main() {
    //2010-2013
    val u01 = "2010-01-01..2013-12-31"
    qryGithubFullscanDateRange(dt(2010, 1, 1), dt(2013, 12, 31))

    //2014
    //val u02 = "2014-01-01..2014-12-31"
    qryGithubFullscanDateRange(dt(2014, 1, 1), dt(2014, 12, 31))

    //2015
    //val u2015a = "2015-01-01..2015-06-30"
    //val u2015b = "2015-07-01..2015-12-31"
    qryGithubFullscanDateRange(dt(2015, 1, 1), dt(2015, 6, 30))
    qryGithubFullscanDateRange(dt(2015, 7, 1), dt(2015, 12, 31))

    //2016
    github2016()
    githubYearBy15Days(2017)
    githubYearBy15Days(2018)
    githubYearBy15Days(2019)
}

private fun dt(year: Int, month: Int, day: Int) = DateTime(year, month, day, 1, 1)
private fun github2016() {
    for (i in 0..5) {
        val s = DateTime(2016, 1 + (i * 2), 1, 1, 1)
        val e = s.plusMonths(2).minusDays(1)
        qryGithubFullscanDateRange(s, e)
    }
}

private fun githubYearBy15Days(year: Int) {
    var s = DateTime(year, 1, 1, 1, 1)
    while (s.year().get() == year) {
        val isFirstOfTheMonth = s.dayOfMonth().get() == 1
        val e = if (isFirstOfTheMonth) s.plusDays(14)!! else s.dayOfMonth().withMaximumValue()!!
        qryGithubFullscanDateRange(s, e)
        s = e.plusDays(1)
    }
}

class DateRange(val s: DateTime, val e: DateTime) {
    val dts = s.toString("yyyy-MM-dd").orEmpty()
    val dte = e.toString("yyyy-MM-dd").orEmpty()
    val qry = "$dts..$dte"
    val fs = "$dts-$dte"
}

private fun qryGithubFullscanDateRange(s: DateTime, e: DateTime) {
    val g = Folders.database.resolve("github").toFile()
    g.mkdirs()

    val dtr = DateRange(s, e)
    val jsonFile = g.resolve("${dtr.fs}.json")

    var page = 1
    qryGitbubDtr(dtr, jsonFile, page)

    val json = jsonFile.readLines().drop(1).joinToString("\n")
    val jsonObject = JsonParser.parseString(json).asJsonObject

    var totalCount = jsonObject.get("total_count").asInt - 100
    while (totalCount > 0) {
        page++
        qryGitbubDtr(dtr, g.resolve("${dtr.fs}--p$page.json"), page)
        totalCount -= 100
    }
}

private fun qryGitbubDtr(dtr: DateRange, jsonFile: File, page: Int) {
    val url = "https://api.github.com/search/repositories?" +
            "q=language:Java+topic:android+is:public+created:${dtr.qry}&page=$page&per_page=100"
    println(url + (if (jsonFile.exists()) " skipping, file already exists" else ""))
    if (!jsonFile.exists()) {
        val content = URL(url).readText()
        jsonFile.writeText("$url\n$content")
        Thread.sleep(20000)
    }
}

private fun githubApiCallOneYear() {
    val g = Folders.database.resolve("github").toFile()
    g.mkdirs()
    var year = 2010;
    while (true) {
        if (year < 2005)
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
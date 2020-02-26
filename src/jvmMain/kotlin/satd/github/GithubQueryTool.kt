package satd.github

import com.google.gson.JsonParser
import org.joda.time.DateTime
import org.joda.time.Interval
import java.io.File
import kotlin.math.ceil
import kotlin.math.max

fun main() {

    val dtStart = DateTime(2000, 1, 1, 0, 0)
    val dtEnd = DateTime(2021, 1, 1, 0, 0)
    GithubQueryTool(
        File("./data/github/queryJavaPublic")
        , DateRange(dtStart, dtEnd)
        , "language:Java is:public"
    )
        .createOutputTxt()
        .also {
            println("Generated: $it contains ${it.useLines { it.count() }}")
        }

}

class GithubQueryTool(workingFolder: File, val dateRange: DateRange, val querySpecification: String) {

    private val cacheFolder = workingFolder.resolve("cache")
    private val jsonFolder = workingFolder.resolve("json")
    private val tokensFile = workingFolder.resolve("github-tokens.txt")
    private val apiCall = GithubApi(tokensFile)
    private val queue = mutableListOf<ReposSearch>()
    private val output = File(workingFolder, "github-url-list.txt")

    fun createOutputTxt(): File {
        cacheFolder.mkdirs()
        jsonFolder.deleteRecursively()
        jsonFolder.mkdirs()

        output.writeText("")

        queue.add(ReposSearch(dateRange, querySpecification))
        while (queue.isNotEmpty()) {
            val search = queue.removeAt(0)
            val result = search.execute()
            if (result.total_count > 1000)
                queue.addAll(0, search.split())
            else {
                result.save()
                search.followingPages(result).forEach { it.execute().save() }
            }
        }
        output.apply { writeText(readLines().toSet().sorted().joinToString("\n")) }
        return output
    }

    inner class ReposSearch(val dateRange: DateRange, val querySpec: String, val page: Int = 1) {
        override fun toString() = "${dateRange.qry} qs=$querySpec page$page"
        private val jsonFile = cacheFolder.resolve("${dateRange.fs}-p$page.json")
        fun execute(): SearchResult {
            val url = "https://api.github.com/search/repositories?" +
                    "q=$querySpec created:${dateRange.qry}&page=$page&per_page=100"
                        .replace(" ", "+")

            val content = apiCall.Call(url, jsonFile).invoke()

            return SearchResult(content)

        }


        fun split(): List<ReposSearch> {
            val (left, right) = dateRange.split()
            return listOf(ReposSearch(left, querySpec), ReposSearch(right, querySpec))
        }

        fun followingPages(r: SearchResult): Sequence<ReposSearch> {
            val pageCount = ceil(r.total_count.toDouble() / 100).toInt()
            if (pageCount <= 1) return emptySequence()
            return (2..pageCount).map { ReposSearch(dateRange, querySpec, it) }.asSequence()
        }

        inner class SearchResult(jsonContent: String) {

            private val json by lazy { JsonParser.parseString(jsonContent).asJsonObject!! }
            val total_count: Int by lazy { json.get("total_count").asInt }

            fun save() {
                jsonFile.copyTo(File(jsonFolder, jsonFile.name))
                json.getAsJsonArray("items")
                    .forEach {
                        val html_url = it.asJsonObject.get("html_url").asString
                        output.appendText("$html_url\n")
                    }
            }
        }
    }


}


class DateRange(val dtStart: DateTime, val dtEnd: DateTime) {
    fun split(): Pair<DateRange, DateRange> {
        if (dtStart == dtEnd) throw Exception("Cannot split one day :(")

        val days1 = days / 2
        val days2 = days - days1
        val days = max(days1, days2) - 1

        val left = DateRange(dtStart, dtStart.plusDays(days))
        val right = DateRange(dtStart.plusDays(days + 1), dtEnd)
        return Pair(left, right)
    }

    val days: Int = Interval(dtStart, dtEnd.plusDays(1)).toDuration().toStandardDays().days

    private fun DateTime.yyyyMMdd() = toString("yyyy-MM-dd").orEmpty()

    val start = dtStart.yyyyMMdd()
    val end = dtEnd.yyyyMMdd()
    val qry = "$start..$end"
    val fs = "$start--$end"

    override fun toString() = qry
}




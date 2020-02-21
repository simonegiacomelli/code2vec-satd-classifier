package satd.github

import com.google.gson.JsonParser
import org.joda.time.DateTime
import org.joda.time.Interval
import java.io.File
import java.net.URL
import kotlin.math.ceil
import kotlin.math.max

fun main() {

    val dtStart = DateTime(2000, 1, 1, 0, 0)
    val dtEnd = DateTime(2021, 1, 1, 0, 0)
    GithubQueryTool(
        File("./data/github/query1")
        , DateRange(dtStart, dtEnd)
        , "language:Java topic:android is:public"
    )
        .createTxt()
        .also {
            println("Generated: $it contains ${it.useLines { it.count() }}")
        }

}

class GithubQueryTool(val workingFolder: File, val dateRange: DateRange, val querySpecification: String) {

    private val jsonFolder = workingFolder.resolve("json")
    private val L = mutableListOf<ReposSearch>()
    private val output = File(workingFolder, "github-url-list.txt")
    fun createTxt(): File {
        jsonFolder.mkdirs()
        output.writeText("")

        L.add(ReposSearch(dateRange, querySpecification))
        while (L.isNotEmpty()) {
            val q = L.removeAt(0)
            val r = q.execute()
            if (r.total_count > 1000)
                L.addAll(0, q.split())
            else {
                r.save()
                q.followingPages(r)
                    .forEach { it.execute().save() }
            }
        }
        return output
    }

    inner class ReposSearch(val dateRange: DateRange, val querySpecification: String, val page: Int = 1) {
        override fun toString() = "${dateRange.qry} qs=$querySpecification page$page"

        private val jsonFile = jsonFolder.resolve("${dateRange.fs}-p$page.json")

        fun execute(): SearchResult {
            val url = "https://api.github.com/search/repositories?" +
                    "q=$querySpecification created:${dateRange.qry}&page=$page&per_page=100"
                        .replace(" ", "+")

            println(url + (if (jsonFile.exists()) " skipping, file already exists" else ""))

            if (!jsonFile.exists()) {
                val content = URL(url).readText()
                jsonFile.writeText("$url\n$content")
                //this could read the headers and wait the minimum amount of time
                //see https://developer.github.com/v3/#rate-limiting
                Thread.sleep(1000)
//                Thread.sleep(20000)
            }

            val content = jsonFile.readLines().drop(1).joinToString("\n")

            return SearchResult(content)

        }

        fun split(): List<ReposSearch> {
            val (left, right) = dateRange.split()
            return listOf(ReposSearch(left, querySpecification), ReposSearch(right, querySpecification))
        }

        fun followingPages(r: SearchResult): Sequence<ReposSearch> {
            val pageCount = ceil(r.total_count.toDouble() / 100).toInt()
            if (pageCount <= 1) return emptySequence()
            return (2..pageCount).map { ReposSearch(dateRange, querySpecification, it) }.asSequence()
        }


    }

    inner class SearchResult(jsonContent: String) {

        private val json by lazy { JsonParser.parseString(jsonContent).asJsonObject!! }
        val total_count: Int by lazy { json.get("total_count").asInt }

        fun save() {
            json.getAsJsonArray("items")
                .forEach {
                    val html_url = it.asJsonObject.get("html_url").asString
                    output.appendText("$html_url\n")
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

    val start = dtStart.yyyyMMdd()
    val end = dtEnd.yyyyMMdd()
    val qry = "$start..$end"
    val fs = "$start-$end"

    override fun toString() = qry
}

private fun DateTime.yyyyMMdd() = toString("yyyy-MM-dd").orEmpty()


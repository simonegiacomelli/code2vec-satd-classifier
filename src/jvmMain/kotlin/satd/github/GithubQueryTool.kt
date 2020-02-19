package satd.github

import org.joda.time.DateTime
import java.io.File

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

class GithubQueryTool(workingFolder: File, val dateRange: DateRange, val querySpecification: String) {

    private val jsonFolder = workingFolder.resolve("json")
    private val L = mutableListOf<ReposSearch>()
    fun createTxt(): File {
        jsonFolder.mkdirs()
        L.add(ReposSearch(dateRange, querySpecification))
        while (L.isNotEmpty()) {
            val q = L.removeAt(0)
            val r = q.execute()
            if (r.hits > 0)
                L.addAll(0, q.split())
            else {
                r.save()
                q.followingPages()
                    .forEach { it.execute().save() }
            }
        }
        return File("")
    }

    class ReposSearch(dateRange: DateRange, querySpecification: String) {
        fun execute(): SearchResult {
            TODO("not implemented")
        }

        fun split(): List<ReposSearch> {
            TODO("not implemented")
        }

        fun followingPages():Sequence<ReposSearch> {
            TODO("not implemented")
        }

    }

    class SearchResult(val hits: Int){

        fun save() {
            TODO("not implemented")
        }
    }

}


class DateRange(val dtStart: DateTime, val dtEnd: DateTime) {
    val start = dtStart.yyyyMMdd()
    val end = dtEnd.yyyyMMdd()
    val qry = "$start..$end"
    val fs = "$start-$end"
}

private fun DateTime.yyyyMMdd() = toString("yyyy-MM-dd").orEmpty()


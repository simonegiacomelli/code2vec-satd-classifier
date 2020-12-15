package satd.github.v4

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.joda.time.DateTime
import satd.github.DateRange
import java.io.File
import kotlin.math.ceil

fun main() {

    val dtStart = DateTime.parse("2000-01-01")
    val dtEnd = DateTime.parse("2019-12-31")
    GithubQueryTool(
        File("./data/github-url-mining/queryJavaPublic-v4-commit")
        , DateRange(dtStart, dtEnd)
        , "language:Java is:public"
    )
        .createOutputTxt()
        .also {
            println("Generated: $it contains ${it.useLines { it.count() }}")
        }

}

class GithubQueryTool(workingFolder: File, val dateRange: DateRange, val querySpec: String) {

    private val cacheFolder = workingFolder.resolve("cache")
    private val tokensFile = workingFolder.resolve("github-tokens.txt")
    private val apiCall = GithubApiV4(tokensFile)
    private val queue = mutableListOf<ReposSearch>()
    private val output = File(workingFolder, "github-url-list.txt")
    private val outputTsv = File(workingFolder, "github-url-list.tsv")

    fun createOutputTxt(): File {
        cacheFolder.mkdirs()

        output.writeText("")
        outputTsv.writeText("")

        queue.add(ReposSearch(Type.PROBE, dateRange))
        while (queue.isNotEmpty()) {
            val search = queue.removeAt(0)
            val probe = search.execute()
            if (probe.repositoryCount > 0)
                if (probe.repositoryCount <= 1000) {
                    val pageCount = ceil(probe.repositoryCount.toDouble() / 100).toInt()
                    val page = execute(search)
                    if (pageCount != page) println("weird $pageCount != $page <-------------------------------")
                } else
                    queue.addAll(0, search.split())

        }
        output.apply { writeText(readLines().toSet().sorted().joinToString("\n")) }
        return output
    }

    private fun execute(search: ReposSearch): Int {
        val result = search.toQuery().execute()
        result.save()
        var res = result
        var page = 1
        while (res.hasNextPage) {
            page++
            res = ReposSearch(Type.QUERY, search.dateRange, page, res.endCursor).execute()
            res.save()
        }
        return page
    }

    enum class Type { PROBE, QUERY }

    inner class ReposSearch(val type: Type, val dateRange: DateRange, val page: Int = 1, val cursor: String = "") {
        override fun toString() = "${dateRange.qry} qs=$querySpec cursor=$cursor"
        private val cacheFile = cacheFolder.resolve("${type.name}-${dateRange.fs}-p$page.json")
        fun execute(): SearchResult {
            val queryJson = when (type) {
                Type.PROBE -> qryRepoProbe(querySpec)
                Type.QUERY -> qryRepoNames(querySpec, cursor)
            }
            val searchResult = apiCall.Call(queryJson, cacheFile
                , { SearchResult(it, this) }).invoke()

            return searchResult

        }


        fun split(): List<ReposSearch> {
            val (left, right) = dateRange.split()
            return listOf(ReposSearch(Type.PROBE, left), ReposSearch(Type.PROBE, right))
        }

        fun toQuery(): ReposSearch = ReposSearch(Type.QUERY, dateRange)

        inner class SearchResult(val jsonContent: String, repoSearch: ReposSearch) : GithubApiV4.Verifier {

            private val json by lazy { JsonParser.parseString(jsonContent).asJsonObject!! }
            private val data by lazy { json.get("data").asJsonObject }
            private val search by lazy { data.get("search").asJsonObject }
            val repositoryCount: Int by lazy { search.get("repositoryCount").asInt }
            private val pageInfo by lazy { search.get("pageInfo").asJsonObject }
            val endCursor by lazy { pageInfo.get("endCursor").asString }
            val hasNextPage by lazy { pageInfo.get("hasNextPage").asBoolean }

            fun save() {
                search.getAsJsonArray("edges")
                    .forEach {
                        val node = it.asJsonObject.get("node").asJsonObject
                        val name = node.get("nameWithOwner").asString
                        val issueCount = node.get("issues").asJsonObject.get("totalCount").asInt
                        val createdAt = node.get("createdAt").asString
                        val commitCount = getCommitCount(node)
                        val line = "$createdAt\t$name\t$issueCount\t${commitCount ?: "null"}\n"
                        outputTsv.appendText(line)
                        apiCall.rate.spin()
                        if (commitCount ?: 0 > 100 || issueCount > 100)
                            output.appendText(line)

                    }
            }

            private fun getCommitCount(node: JsonObject): Int? {
                val obj = node.get("object")
                if (obj.isJsonNull) return null
                return obj.asJsonObject.get("history").asJsonObject.get("totalCount").asInt
            }

            override var exception: Exception? = null

            override fun acceptable(): Boolean {
                exception = null
                return try {
                    //just hit the parsing
                    repositoryCount
                    true
                } catch (ex: Exception) {
                    exception = ex
                    false
                }
            }

        }
    }


}


fun GithubQueryTool.ReposSearch.qryRepoNames(querySpec: String, cursor: String): String {
    val cursorClause = if (cursor.isEmpty()) "" else "after:\"$cursor\""
    return """query q1 {
search(query: "$querySpec created:${dateRange.qry}", type: REPOSITORY, first: 100 $cursorClause) {
repositoryCount
edges {
  node {
    ... on Repository {
      nameWithOwner
      createdAt
      diskUsage
      issues {
        totalCount
      }
      
      object(expression:"master") {
        ... on Commit {
          history {
            totalCount
          }
        }
      }
#      refs(first: 3, refPrefix: "refs/heads/") {
#        edges {
#          node {
#            name
#            target {
#              ... on Commit {
#                history(first: 0) {
#                  totalCount
#                }
#              }
#            }
#          }
#        }
#      }
    }
  }
 # cursor
}
pageInfo {
  endCursor
  hasNextPage
}
}
}
"""
}


fun GithubQueryTool.ReposSearch.qryRepoProbe(querySpec: String): String = """query q1 {
  rateLimit {
    limit
    cost
    remaining
    resetAt
  }
  search(query: "$querySpec created:${dateRange.qry}", type: REPOSITORY, first: 100) {
    repositoryCount
  }
}
"""

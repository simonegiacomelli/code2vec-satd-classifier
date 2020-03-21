package satd.github.v4

import org.joda.time.DateTime
import satd.github.DateRange
import java.io.File

fun main() {

    val dtStart = DateTime.parse("2000-01-01")
    val dtEnd = DateTime.parse("2019-12-31")
    GithubQueryTool(
        File("./data/github-url-mining/queryJavascriptPublic-v4")
        , DateRange(dtStart, dtEnd)
        , "language:Javascript is:public"
    )
        .createOutputTxt()
        .also {
            println("Generated: $it contains ${it.useLines { it.count() }}")
        }

}
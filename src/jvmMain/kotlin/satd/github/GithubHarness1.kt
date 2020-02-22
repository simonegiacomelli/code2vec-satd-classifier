package satd.github

import java.net.URL

fun main() {
    val url =
        "https://api.github.com/search/repositories?q=language:Java+topic:android+is:public+created:2015-02-05..2015-10-02&page=10&per_page=100"
//    val url  = "https://c.jako.pro/laz/"
    val c = URL(url).openConnection()
    //{X-RateLimit-Reset=[1582281440], X-RateLimit-Remaining=[7], X-RateLimit-Limit=[10]}
    println(c.headerFields.filter { it.key.orEmpty().contains("rate", ignoreCase = true) })
    val content = c.getInputStream().use { it.readBytes() }.toString(Charsets.UTF_8)
    println(url)
    println(content)
}
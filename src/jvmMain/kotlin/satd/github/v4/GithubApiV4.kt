package satd.github.v4

import org.joda.time.DateTime
import org.joda.time.Period
import satd.utils.Rate
import java.io.File
import java.net.URL
import java.net.URLConnection
import java.util.*

class GithubApiV4(val tokensFile: File) {

    private var tokensIndex = 0
    private val rate = Rate(120)

    inner class Call(private val url: String, private val jsonFile: File) {

        fun invoke(): String {
            if (jsonFile.exists())
                println("$url skipping, file already exists")
            else
                invokeRetry()
            return jsonFile.readLines().drop(1).joinToString("\n")
        }

        private fun invokeRetry() {
            repeat(10) {
                try {
                    val retry = if (it == 0) "" else "retry $it"
                    println("$url request... $retry")
                    invokeUnsafe()
                    return
                } catch (ex: Exception) {
                    println("$url exception:")
                    ex.printStackTrace()
                    Thread.sleep(1000)
                }
            }
        }

        private fun invokeUnsafe() {
            val c = URL(url).openConnection()
            manageAuthorization(c)
            //see https://developer.github.com/v3/#rate-limiting
            //e.g. {X-RateLimit-Reset=[1582281440], X-RateLimit-Remaining=[7], X-RateLimit-Limit=[10]}

            val headers = c.headerFields
                .filter { it.key.orEmpty().contains("rate", ignoreCase = true) }
                .mapValues { it.value.first().orEmpty() }
                .mapKeys { it.key }

            print("req/sec:$rate headers: $headers")
            val resetDt = DateTime((headers["X-RateLimit-Reset"] ?: "").toLong() * 1000).toLocalDateTime()
            val resetSecs = Period(DateTime(), resetDt.toDateTime()).toStandardSeconds().seconds
            println("reset on: $resetDt in $resetSecs secs")
            val content = c.getInputStream().use { it.readBytes() }.toString(Charsets.UTF_8)

            rate.spin()

            jsonFile.writeText("$url\n$content")
            //this could read the headers and wait the minimum amount of time
            if ((headers["X-RateLimit-Remaining"] ?: "").toInt() <= 1) {
                val l = resetSecs.toLong() * 1000
                print(" sleeping for $l")
                Thread.sleep(l)
            }
        }
    }

    private fun manageAuthorization(c: URLConnection) {

        if (!tokensFile.exists())
            tokensFile.writeText("#expeected format is username:token\n#see https://developer.github.com/v3/auth/#basic-authentication")

        val lines = tokensFile.readLines().filterNot { it.startsWith("#") or it.isBlank() }
        if (lines.isEmpty())
            return

        tokensIndex %= lines.size

        val userCredentials = lines[tokensIndex]
        val basicAuth = "Basic " + String(Base64.getEncoder().encode(userCredentials.toByteArray()))
        c.setRequestProperty("Authorization", basicAuth)
        tokensIndex++
    }

}

package satd.github.v4

import org.joda.time.DateTime
import org.joda.time.Period
import satd.utils.Rate
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.*

class GithubApiV4(val tokensFile: File) {

    private var tokensIndex = 0
    private val rate = Rate(120)

    inner class Call(private val queryDescription: String, private val queryJson: String, private val jsonFile: File) {

        fun invoke(): String {
            if (jsonFile.exists())
                println("$queryDescription skipping, file already exists")
            else
                invokeRetry()

            return jsonFile.readLines().drop(1).joinToString("\n")
        }

        private fun invokeRetry() {
            repeat(10) {
                try {
                    val retry = if (it == 0) "" else "retry $it"
                    println("$queryDescription request... $retry")
                    invokeUnsafe()
                    return
                } catch (ex: Exception) {
                    println("$queryDescription exception:")
                    ex.printStackTrace()
                    Thread.sleep(1000)
                }
            }
        }

        private fun invokeUnsafe() {

            //https://developer.github.com/v4/guides/forming-calls/#communicating-with-graphql
            val c = URL("https://api.github.com/graphql").openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.doOutput = true

            //https://developer.github.com/v4/guides/resource-limitations/
            manageAuthorization(c)

            c.outputStream.bufferedWriter().use { it.write(queryJson) }

            val content = c.getInputStream().use { it.readBytes() }.toString(Charsets.UTF_8)

            rate.spin()

            jsonFile.writeText("$queryJson\n$content")
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

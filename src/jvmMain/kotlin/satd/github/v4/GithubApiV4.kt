package satd.github.v4

import com.google.gson.JsonObject
import satd.utils.Rate
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.*


class GithubApiV4(val tokensFile: File) {

    private var tokensIndex = 0
    private val rate = Rate(120)

    inner class Call(private val queryJson: String, private val jsonFile: File) {

        fun invoke(): String {
            if (jsonFile.exists())
                println("${jsonFile.name} skipping, file already exists")
            else
                invokeRetry()

            return jsonFile.readText()
        }

        private fun invokeRetry() {
            repeat(50) {
                try {
                    val retry = if (it == 0) "" else "retry $it"
                    println("${jsonFile.name} request... $retry")
                    invokeUnsafe()
                    return
                } catch (ex: Exception) {
                    if (ex.doNotCatch())
                        throw ex
                    println("${jsonFile.name} exception:")
                    ex.printStackTrace()
                    Thread.sleep(if (it <= 10) 1000 else 10000)
                }
            }
        }

        private fun invokeUnsafe() {

            //https://developer.github.com/v4/guides/forming-calls/#communicating-with-graphql
            val c = URL("https://api.github.com/graphql").openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.doOutput = true
            c.doInput = true

            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            c.setRequestProperty("charset", "utf-8");

            //https://developer.github.com/v4/guides/resource-limitations/
            manageAuthorization(c)
            val requestFile = File(jsonFile.path + ".request.txt")
            requestFile.writeText(queryJson)

            val jostr = JsonObject().apply { addProperty("query", queryJson) }.toString()
            requestFile.appendText("\n\n$jostr")
            c.outputStream.bufferedWriter().use { it.write(jostr); it.flush() }

            if (c.responseCode == HttpURLConnection.HTTP_OK) {
                val content = c.inputStream.use { it.readBytes() }.toString(Charsets.UTF_8)
                rate.spin()
                jsonFile.writeText(content)
            } else {
                val err = c.errorStream.use { it.bufferedReader().readText() }
                requestFile.appendText("\n\n$err")
                throw Exception("code: ${c.responseCode} errorStream=$err")
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

private fun java.lang.Exception.doNotCatch(): Boolean {
    val msg = message.orEmpty().toLowerCase()
    return msg.contains("code: 401") || msg.contains("code: 400")
}

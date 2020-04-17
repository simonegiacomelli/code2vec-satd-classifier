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
    val rate = Rate(60)

    interface Verifier {
        fun acceptable(): Boolean
        val exception: Exception?
    }


    inner class Call<T : Verifier>(
        private val queryJson: String,
        private val jsonFile: File,
        val verifier: (String) -> T
    ) {

        fun invoke(): T {
            if (jsonFile.exists())
                verifier(jsonFile.readText()).also {
                    if (it.acceptable()) {
                        println("${jsonFile.name} skipping, file already exists")
                        return it
                    } else
                        println("${jsonFile.name} Content was not acceptable error: ${it.exception}")
                }

            return invokeRetry()
        }

        private fun invokeRetry(): T {
            repeat(50) {
                try {
                    val retry = if (it == 0) "" else "retry $it"
                    print("url-repo/sec:$rate  ${jsonFile.name} request... $retry")
                    return invokeUnsafe()
                } catch (ex: Exception) {
                    if (ex.doNotCatch())
                        throw ex
                    println("${jsonFile.name} exception:")
                    ex.printStackTrace()
                    Thread.sleep(if (it <= 10) 1000 else 10000)
                }
            }
            throw Exception("Retry exhausted")
        }

        private fun invokeUnsafe(): T {

            //https://developer.github.com/v4/guides/forming-calls/#communicating-with-graphql
            val c = URL("https://api.github.com/graphql").openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.doOutput = true
            c.doInput = true
            c.setConnectTimeout(60000);
            c.setReadTimeout(60000);

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
                verifier(content).also {
                    if (!it.acceptable())
                        throw Exception("Content was not acceptable")
                    jsonFile.writeText(content)
                    println("done")
                    return it
                }
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

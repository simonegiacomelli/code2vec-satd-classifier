package satd.step2

import satd.utils.Folders
import satd.utils.dateTimeToStr
import java.io.PrintWriter
import java.io.StringWriter


class Exceptions(val ex: Throwable, val repoName: String) {

    fun handle() {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        val sStackTrace = sw.toString() // stack trace as a string

        Folders.log.resolve("run/${dateTimeToStr()}/")
            .also { it.toFile().mkdirs() }
            .resolve(repoName + "__exception.txt")
            .toFile()
            .writeText(sStackTrace)

    }

}

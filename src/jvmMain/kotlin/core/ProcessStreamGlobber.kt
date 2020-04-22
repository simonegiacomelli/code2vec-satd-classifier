package core

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/* Simone 08/07/2014 12:47 */   class ProcessStreamGlobber(private val process: Process) {
    private var name: String? = null
    fun startGlobber() {
        startStreamGobbler(process.errorStream, "ERR")
        startStreamGobbler(process.inputStream, "OUT")
    }

    private fun startStreamGobbler(errorStream: InputStream, streamName: String) {
        val sg =
            StreamGobbler(errorStream, streamName)
        if (name != null) sg.name = "$name-$streamName"
        sg.start()
    }

    fun setName(name: String?) {
        this.name = name
    }

    internal inner class StreamGobbler(var `is`: InputStream, var type: String) :
        Thread() {
        override fun run() {
            try {
                val isr = InputStreamReader(`is`)
                val br = BufferedReader(isr)
                var line: String
                while (br.readLine()
                        .also { line = it } != null
                ) log.info("$type> $line")
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        }

    }

    companion object {
        val log = LoggerFactory.getLogger(ProcessStreamGlobber::class.java)
    }

}
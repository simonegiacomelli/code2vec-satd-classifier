package core

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/* Simone 08/07/2014 12:47 */   class ProcessStreamGlobber(private val process: Process, val processName: String = "") {

    fun startGlobber() {
        startStreamGobbler(process.errorStream, "ERR")
        startStreamGobbler(process.inputStream, "OUT")
    }

    private fun startStreamGobbler(errorStream: InputStream, streamName: String) {
        val sg =
            StreamGobbler(errorStream, streamName)
        if (processName.isNotEmpty()) sg.name = "$processName-$streamName"
        sg.start()
    }

    internal inner class StreamGobbler(val input: InputStream, val type: String) :
        Thread() {
        override fun run() {
            input
                .bufferedReader()
                .lines()
                .filter { it != null }
                .forEach { log.info("$type> $it") }
        }

        init {
            isDaemon = true
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ProcessStreamGlobber::class.java)
    }

}
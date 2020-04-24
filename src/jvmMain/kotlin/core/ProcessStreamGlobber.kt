package core

import org.slf4j.LoggerFactory
import java.io.InputStream

/* Simone 08/07/2014 12:47 */

class ProcessStreamGlobber(private val process: Process, val processName: String = "") {
    companion object {
        private val log = LoggerFactory.getLogger(ProcessStreamGlobber::class.java)
        fun handleIS(inputStream: InputStream, type: String) {
            inputStream
                .bufferedReader()
                .lines()
                .filter { it != null }
                .forEach { log.info("$type> $it") }
        }
    }

    fun startGlobber() {
        startStreamGobbler(process.errorStream, "ERR")
        startStreamGobbler(process.inputStream, "OUT")
    }

    private fun startStreamGobbler(errorStream: InputStream, streamName: String) {
        val sg = StreamGobbler(errorStream, streamName)
        if (processName.isNotEmpty()) sg.name = "$processName-$streamName"
        sg.start()
    }

    internal inner class StreamGobbler(val input: InputStream, val type: String) :
        Thread() {
        override fun run() {
            handleIS(input, type)
        }

        init {
            isDaemon = true
        }
    }


}
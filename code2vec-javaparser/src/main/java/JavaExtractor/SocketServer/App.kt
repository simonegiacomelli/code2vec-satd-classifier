package JavaExtractor.SocketServer

import JavaExtractor.Common.CommandLineValues
import JavaExtractor.ExtractFeaturesTask
import JavaExtractor.FeatureExtractor
import JavaExtractor.FeaturesEntities.ProgramFeatures
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.util.ArrayList
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

private val args = CommandLineValues(
    *"--max_path_length 8 --max_path_width 2 --dir ../code2vec-satd/build-dataset/java-small/one --num_threads 10".split(
        " "
    ).toTypedArray()
)

fun main() {
    val server = ServerSocket(9999)
    println("Server is running on port ${server.localPort}")
    while (true) {
        val client = server.accept()
        thread(isDaemon = true) { ClientHandler(client).serve() }
    }
}

class ClientHandler(val client: Socket) {
    val out = DataOutputStream(client.getOutputStream())
    val inp = DataInputStream(client.getInputStream())
    fun serve() {
        client.soTimeout = 5*60*1000
        while (true) {
            val id = inp.readChunkedString()
            val code = inp.readChunkedString()
            val str = try {
                MeasureTimeMillis {
                    featuresToString(FeatureExtractor(args).extractFeatures(code))
                }.apply {
                    if (millis > 3000) println("id=$id took $millis ms")
                }.result
            } catch (ex: Exception) {
                val msg = "FAILED\t" + ex.toString().replace("\n", "\\n")
                println("$id $msg")
                msg
            }
            out.sendStringInChunk(str)
        }
    }
}

private fun featuresToString(features: ArrayList<ProgramFeatures>?) = if (features == null) {
    "FAILED\tNULL"
} else {
    val toPrint = ExtractFeaturesTask.featuresToString(features, args)
    if (toPrint.isNotEmpty()) {
        "OK\t$toPrint"
    } else "FAILED\ttoPrint.length()==0"
}

private fun DataOutputStream.sendStringInChunk(source: String) {
    val chunk = source.chunked(1024 * 32)
    writeInt(chunk.size)
    chunk.forEach { writeUTF(it) }
    flush()
}

private fun DataInputStream.readChunkedString(): String {
    val chunkCount = readInt()
    val code = (1..chunkCount).joinToString("") { readUTF() }
    return code
}

class MeasureTimeMillis<T>(block: () -> T) {
    val millis: Long
    val result: T

    init {
        val start = System.currentTimeMillis()
        result = block()
        millis = System.currentTimeMillis() - start
    }
}

public inline fun <T> measureTimeMillis2(block: () -> T): Pair<Long, T> {
    val start = System.currentTimeMillis()
    val res = block()
    return Pair(System.currentTimeMillis() - start, res)
}

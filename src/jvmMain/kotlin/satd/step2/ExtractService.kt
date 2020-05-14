package satd.step2

import satd.utils.config
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.Socket

fun main() {

    Partitions.all
//    arrayOf("one")
        .forEach { folder ->
            val out = File(config.dataset_export_path, "$folder-features.txt").bufferedWriter()
            File(config.dataset_export_path, folder)
                .listFiles()
                .orEmpty()
                .filter { !it.endsWith(".java") }
                .sortedBy { it.name }
                .forEach {
                    println("Doing $it")
                    val features = ExtractService().extract(it.name, it.readText())
                    println(features)
                    out.appendln("${it.name} $features")
//                readLine()
                }
            out.close()
        }
}


class ExtractService {
    val s = Socket("localhost", 9999).also { it.soTimeout = 30000 }
    val out by lazy { DataOutputStream(s.getOutputStream()) }
    val inp by lazy { DataInputStream(s.getInputStream()) }

    fun extract(id: String, source: String): String {
        out.sendStringInChunk(id)
        out.sendStringInChunk(source)
        return inp.readChunkedString()
    }
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
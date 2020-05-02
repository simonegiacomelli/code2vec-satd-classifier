package satd.step2.perf

import satd.utils.Folders
import satd.utils.config
import java.io.File
import kotlin.math.round
import kotlin.streams.toList

class Dataset {
    val folder: File =
        File(config.dataset_export_path ?: Folders.dataset.resolve("java-small").toAbsolutePath().toString())
    private val evaluatedTest: File = File("$folder-evaluated/test")

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Dataset().apply {
                readEvaluation()
            }
        }


    }

    private fun readEvaluation() {
        val evaluation = evaluatedTest
            .listFiles()
            .orEmpty()
            .filter { it.name.endsWith(".java") }
            .map { file ->
                val lines = file.bufferedReader(bufferSize = 64).lines().skip(1).limit(2).toList().filterNotNull()
                val map = lines
                    .filter { it.contains(":") }
                    .associate { line ->
                        line.split(":").let { Pair(it[0].trim(), it[1].trim()) }
                    }

                val p = Prediction(
                    file.name.split("_")[1].toInt(),
                    map["Actual"] ?: error("Should contain Actual"),
                    map["Prediction"] ?: error("Should contain Prediction")
                )
                println(p)
                p
            }

        val done = evaluation.size
        val correct = evaluation.count { it.actual == it.prediction }
        val acc = round(correct.toDouble() / done * 1000) / 10
        println("correct/done: $correct/$done accuracy: $acc %")

    }

}

data class Prediction(val satdId: Int, val actual: String, val prediction: String)
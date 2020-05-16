package satd.step2

import java.io.File
import kotlin.streams.toList

fun extractPrediction(file: File): Prediction {
    val lines = file.bufferedReader(bufferSize = 64).lines().skip(1).limit(3).toList().filterNotNull()
    val map = lines.take(2)
        .filter { it.contains(":") }
        .associate { line ->
            line.split(":").let { Pair(it[0].trim(), it[1].trim()) }
        }
    val type = map["Actual"] ?: error("Should contain Actual")
    val prerdictedClass = map["Prediction"] ?: error("Should contain Prediction")
    val split = lines.drop(2).first().split(" ")
    val v = split.first().trim().removePrefix("(").removeSuffix(")")
    val sample: Sample =
        Sample.fromFilename(file.name)
    assert2(type == sample.type)
    val pred = Prediction(sample, prerdictedClass, v.toDouble())
    return pred
}

data class Prediction(val sample: Sample, val prediction: String, val confidence: Double) {
    val correct = sample.type == prediction
}

data class Sample(val satdId: Long, val type: String, val index: Int) {
    fun filename(): String = index.toString().padStart(6, '0') +
            "_" + satdId.toString().padStart(6, '0') +
            "_$type.java"

    companion object {
        fun fromFilename(name: String): Sample {
            val nameParts = name.substringBeforeLast(".").split("_")
            return Sample(
                nameParts[1].toLong(),
                nameParts[nameParts.size - 1],
                nameParts[0].toInt()
            )
        }
    }
}
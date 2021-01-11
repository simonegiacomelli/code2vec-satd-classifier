package satd.step2

import java.io.File
import kotlin.streams.toList

fun extractPrediction(file: File): Prediction {
    val lines = file.bufferedReader(bufferSize = 64).use { it.lines().skip(1).limit(40).toList().filterNotNull() }
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
    val idx = lines.indexOf("Attention:")
    val attentions: List<Attention> = if (idx >= 0) {
        val attList = lines.drop(idx).map { it.split("\tcontext: ") }.filter { it.size == 2 }
        attList.map {
            Attention(it[0].toDouble(), it[1])
        }
    } else emptyList()
    val pred = Prediction(sample, prerdictedClass, v.toDouble(), attentions)
    return pred
}

data class Prediction(
    val sample: Sample,
    val prediction: String,
    val confidence: Double,
    val attentions: List<Attention> = emptyList()
) {
    val correct = sample.type == prediction
}

fun List<Attention>.att_ratio(): Double = if (size > 1) this[0].weight / this[1].weight else 0.0

data class Attention(val weight: Double, val context: String)

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
package satd.step2

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Folders
import satd.utils.config
import java.io.File
import kotlin.math.round
import kotlin.streams.toList

class MainImportPredictions {
    val folder: File =
        File(config.dataset_export_path ?: Folders.dataset.resolve("java-small").toAbsolutePath().toString())
    private val evaluatedTest: File = File("$folder-evaluated/test")

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MainImportPredictions().apply {
                //evaluationPrint()
                persistence.setupDatabase()
                evaluationCopyInDb()
            }
        }
    }

    private fun Boolean.toShort(): Short {
        return if (this) 1 else 0
    }

    fun evaluationCopyInDb() {
        val id: Int = transaction { DbRuns.nextId() }
        transaction {
            sequence {
                val s = evaluation.asIterable().iterator()
                while (s.hasNext()) {
                    yield(Pair(s.next(), s.next()))
                }
            }.forEach { (old, new) ->
                assert2(old.first.satdId == new.first.satdId)
                assert2(old.first.index == (new.first.index - 1))
                val satdOk = (old.first.type == old.second).toShort()
                val fixedOk = (new.first.type == new.second).toShort()
                println("satd_id=${old.first.satdId} satd_ok=$satdOk fixed_ok=$fixedOk")
                DbEvals.insert {
                    it[run_id] = id
                    it[satd_id] = old.first.satdId
                    it[satd_ok] = satdOk
                    it[fixed_ok] = fixedOk
                }
            }
        }

    }

    fun evaluationPrint() {
        evaluation.forEach { println(it) }
        val done = evaluation.size
        val correct = evaluation.count { it.first.type == it.second }
        val acc = round(correct.toDouble() / done * 1000) / 10
        println("correct/done: $correct/$done accuracy: $acc %")
    }

    val evaluation: List<Pair<Sample, String>> by lazy {
        evaluatedTest
            .listFiles()
            .orEmpty()
            .filter { it.name.endsWith(".java") }
            .sorted()
            .map { file ->
                val lines = file.bufferedReader(bufferSize = 64).lines().skip(1).limit(2).toList().filterNotNull()
                val map = lines
                    .filter { it.contains(":") }
                    .associate { line ->
                        line.split(":").let { Pair(it[0].trim(), it[1].trim()) }
                    }
                val type = map["Actual"] ?: error("Should contain Actual")
                val prediction = map["Prediction"] ?: error("Should contain Prediction")

                val nameParts = file.name.split("_")
                val p = Sample(
                    nameParts[1].toLong(),
                    type,
                    nameParts[0].toInt()
                )
                Pair(p, prediction)
            }
    }

}

data class Sample(val satdId: Long, val type: String, val index: Int)
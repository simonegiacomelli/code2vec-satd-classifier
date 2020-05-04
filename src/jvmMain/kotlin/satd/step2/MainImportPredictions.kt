package satd.step2

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import satd.step2.perf.Prediction
import satd.step2.perf.extractPrediction
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
                assert2(old.sample.satdId == new.sample.satdId)
                assert2(old.sample.index == (new.sample.index - 1))
                val satdOk = old.correct.toShort()
                val fixedOk = new.correct.toShort()
                println("satd_id=${old.sample.satdId} satd_ok=$satdOk fixed_ok=$fixedOk")
                DbEvals.insert {
                    it[run_id] = id
                    it[satd_id] = old.sample.satdId
                    it[satd_ok] = satdOk
                    it[fixed_ok] = fixedOk
                    it[satd_confidence] = old.confidence
                    it[fixed_confidence] = new.confidence
                }
            }
        }

    }

    fun evaluationPrint() {
        evaluation.forEach { println(it) }
        val done = evaluation.size
        val correct = evaluation.count { it.correct }
        val acc = round(correct.toDouble() / done * 1000) / 10
        println("correct/done: $correct/$done accuracy: $acc %")
    }

    val evaluation: List<Prediction> by lazy {
        evaluatedTest
            .listFiles()
            .orEmpty()
            .filter { it.name.endsWith(".java") }
            .sorted()
            .map { file -> extractPrediction(file) }
    }

}

data class Sample(val satdId: Long, val type: String, val index: Int)
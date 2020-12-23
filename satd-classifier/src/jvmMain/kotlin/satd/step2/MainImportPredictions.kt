package satd.step2

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.config
import java.io.File
import kotlin.math.round

class MainImportPredictions {
    val folder: File = File(config.dataset_export_path).absoluteFile.normalize()
    val infoFile = folder.resolve("info.txt")
    val outputFile = folder.resolve("output.txt")
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
        val id: Int = transaction { DbRuns.newRun(DatasetInfo.loadFrom(infoFile), result, outputFile.readText()) }
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
                    it[satd_att_ratio] = old.attentions.att_ratio()
                    it[fixed_att_ratio] = new.attentions.att_ratio()
                }
            }
        }

    }

    val result by lazy {
        val done = evaluation.size
        val correct = evaluation.count { it.correct }
        val accuracy = round(correct.toDouble() / done * 10000) / 100
        Result(done, correct, accuracy)
    }


    fun evaluationPrint() {
        evaluation.forEach { println(it) }
        result.apply { println("correct/done: $correctCount/$totalCount accuracy: $accuracy %") }
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

data class Result(val totalCount: Int, val correctCount: Int, val accuracy: Double)

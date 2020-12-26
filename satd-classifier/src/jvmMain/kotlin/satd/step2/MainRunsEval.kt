package satd.step2

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import kotlin.math.round

fun main() {
    persistence.setupDatabase()

    val header = "run_id,positive_class,confidence,tp,tn,fp,fn,precision,recall,accuracy,f1".replace(",", "\t")
    val confidences = listOf(0.5, 0.6, 0.7, 0.8, 0.9, 0.0)

    fun Double.r() = round(this * 10000) / 100

    transaction {
//        val runIdList = DbRuns.run { slice(id).selectAll().map { it[id].value } }.toList()
        val runIdList = listOf(7)
        val lines = runIdList.map { runId ->
            val expected = DbRuns.run { slice(test_count).select { id.eq(runId) }.first()[test_count] }
            val actual = DbEvals.run { select { run_id.eq(runId) }.count() }
            assert2(expected == actual) { "run_id=$runId DbEvals row count expected=$expected actual=$actual" }
            confidences.map { confidence ->
                val rs = connection.query(sql1(runId, confidence)).toList()
                val map = rs.map { "${it[0]}-${it[1]}" to (it[2] as Number).toInt() }.toMap()
                listOf("satd" to "fixed", "fixed" to "satd").map {
                    val posi = it.first
                    val nega = it.second
                    val tp = map["$posi-1"] ?: 0
                    val tn = map["$nega-1"] ?: 0
                    val fp = map["$posi-0"] ?: 0
                    val fn = map["$nega-0"] ?: 0
                    RelevanceMeasures(tp, tn, fp, fn).run {
                        "$runId,$posi,$confidence,$tp,$tn,$fp,$fn,${precision.r()},${recall.r()},${accuracy.r()},${f1.r()}"
                            .replace(",", "\t")
                    }
                }
            }.flatten()
        }.flatten()

        val tsv = listOf(header, *lines.toTypedArray()).joinToString("\n")
        println("")
        println(tsv)
        val stringSelection = StringSelection(tsv)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, stringSelection)
    }
}

class MainRunsEvalAdjusted {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MainRunsEvalAdjusted().main()
        }
    }

    fun main() {
        persistence.setupDatabase()

        val confidences = listOf(0.0, 0.5, 0.6, 0.7, 0.8, 0.9)

        transaction {
            val runId = 7

            val query = DbEvals.run {
                slice(
                    satd_id,
                    satd_ok,
                    fixed_ok,
                    satd_confidence,
                    fixed_confidence
                ).select { run_id.eq(runId) }
            }

            val expected = DbRuns.run { slice(test_count).select { id.eq(runId) }.first()[test_count] }
            val actual = DbEvals.run { select { run_id.eq(runId) }.count() }
            assert2(expected == actual) { "run_id=$runId DbEvals row count expected=$expected actual=$actual" }
            data class Eval(val posiClass: String, val confidenceLevel: Double) {
                val metrics = mutableListOf(0, 0, 0, 0, 0) // tp, tn, fp, fn, discarded_fn
                fun accumulate(isPosiClass: Boolean, correct: Boolean, confidence: Double) {
                    val index = (if (correct) 0 else 2) + (if (isPosiClass) 0 else 1)
                    if (confidence >= confidenceLevel)
                        metrics[index] += 1
                    else
                        if (index == 3) //# adjust for recall definition; we account for discarded wrong
                            metrics[4] += 1
                }


            }

            val evals = listOf("satd", "fixed").map { positiveClass ->
                confidences.map { confidence ->
                    val eval = Eval(positiveClass, confidence)
                    DbEvals.run {
                        query.map { row ->
                            eval.accumulate(positiveClass == "satd", row[satd_ok].toInt() == 1, row[satd_confidence])
                            eval.accumulate(positiveClass == "fixed", row[fixed_ok].toInt() == 1, row[fixed_confidence])
                        }
                    }
                    RelevanceMeasuresAdjusted(positiveClass, confidence, eval.metrics)
                }

            }.flatten()


            val tsv = listOf(
                "runId\t" + RelevanceMeasuresAdjusted.tsvHeader,
                *(evals.map { "$runId\t" + it.round().tsv }.toTypedArray())
            ).joinToString("\n")
            println("")
            println(tsv)
            val stringSelection = StringSelection(tsv)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, stringSelection)
        }
    }

}

class RelevanceMeasures(val tp: Int, val tn: Int, val fp: Int, val fn: Int) {
    val precision = tp / (tp + fp).toDouble()
    val recall = tp / (tp + fn).toDouble()
    val accuracy = (tp + tn) / (tp + tn + fp + fn).toDouble()
    val f1 = 2 * (precision * recall) / (precision + recall)
}

data class RelevanceMeasuresAdjusted(
    val positiveClass: String,
    val confidence: Double,
    val tp: Int, val tn: Int, val fp: Int, val fn: Int, val discarded_fn: Int,
    val precision: Double = tp / (tp + fp).toDouble(),
    val recall: Double = tp / (tp + fn).toDouble(),
    val recall_adjusted: Double = tp / (tp + fn + discarded_fn).toDouble(),
    val accuracy: Double = (tp + tn) / (tp + tn + fp + fn + discarded_fn).toDouble(),
    val f1: Double = 2 * (precision * recall_adjusted) / (precision + recall_adjusted)
) {
    constructor(positiveClass: String, confidence: Double, p: List<Int>) : this(
        positiveClass, confidence,
        p[0], p[1], p[2], p[3], p[4]
    )

    companion object {
        private val String.commaToTab get() = this.replace(",", "\t")
        val tsvHeader =
            "positive_class,confidence,tp,tn,fp,fn,discarded_fn,precision,recall,recall_adjusted,accuracy,f1".commaToTab
    }

    val tsv =
        "$positiveClass,$confidence,$tp,$tn,$fp,$fn,$discarded_fn,$precision,$recall,$recall_adjusted,$accuracy,$f1".commaToTab


    fun round() = RelevanceMeasuresAdjusted(
        positiveClass, confidence, tp, tn, fp, fn, discarded_fn,
        precision.r, recall.r, recall_adjusted.r, accuracy.r, f1.r
    )

    private val Double.r get() = round(this * 1000) / 10
}

private fun Sequence<Array<Any>>.print() = forEach { println("  " + it.joinToString("\t")) }


private fun sql1(runId: Int, confidence: Double) = """
    
SELECT   'satd' kind,
       satd_ok,
       COUNT(*)
FROM dbevals
WHERE  run_id = $runId
AND   satd_confidence >= $confidence
GROUP BY 1,
         2
union 
SELECT   'fixed' kind,
       fixed_ok,
       COUNT(*)
FROM dbevals
WHERE  run_id = $runId
AND   fixed_confidence >=  $confidence
GROUP BY 1,
         2
""".trimIndent()
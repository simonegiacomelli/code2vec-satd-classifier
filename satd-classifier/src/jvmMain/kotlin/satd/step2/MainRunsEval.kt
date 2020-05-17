package satd.step2

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.round

fun main() {
    persistence.setupDatabase()

    val header = "run_id,positive_class,confidence,precision,recall,accuracy,f1".replace(",", "\t")
    val confidences = listOf(0.5, 0.6, 0.7, 0.8, 0.9)

    transaction {
        val runIdList = DbRuns.run { slice(id).selectAll().map { it[id].value } }.toList()
        val lines = runIdList.map { runId ->
            val expected = DbRuns.run { slice(test_count).select { id.eq(runId) }.first()[test_count] }
            val actual = DbEvals.run { select { run_id.eq(runId) }.count() }
            assert2(expected == actual) { "run_id=$runId DbEvals row count expected=$expected actual=$actual" }
            confidences.map { confidence ->
                val rs = connection.query(sql1(confidence)).toList()
                val map = rs.map { "${it[0]}-${it[1]}" to (it[2] as Number).toInt() }.toMap()
                listOf("satd" to "fixed", "fixed" to "satd").map {
                    val posi = it.first
                    val nega = it.second
                    val tp = map["$posi-1"] ?: 0
                    val fn = map["$posi-0"] ?: 0
                    val tn = map["$nega-1"] ?: 0
                    val fp = map["$nega-0"] ?: 0
                    RelevanceMeasures(tp, tn, fp, fn).run {
                        "$runId,$posi,$confidence,${precision.rounded},${recall.rounded},${accuracy.rounded},${f1.rounded}"
                            .replace(",", "\t")
                    }
                }
            }.flatten()
        }.flatten()

        println("")
        println(header)
        lines.forEach { println(it) }

    }
}

private val Double.rounded get() = round(this * 10000) / 100

class RelevanceMeasures(val tp: Int, val tn: Int, val fp: Int, val fn: Int) {

    val precision = tp / (tp + fp).toDouble()
    val recall = tp / (tp + fn).toDouble()
    val accuracy = (tp + tn) / (tp + tn + fp + fn).toDouble()
    val f1 = 2 * (precision * recall) / (precision + recall)


}

private fun Sequence<Array<Any>>.print() = forEach { println("  " + it.joinToString("\t")) }


private fun sql1(confidence: Double) = """
    
SELECT   'satd' kind,
       satd_ok,
       COUNT(*)
FROM dbevals
WHERE  run_id = 4
AND   satd_confidence >= $confidence
GROUP BY 1,
         2
union 
SELECT   'fixed' kind,
       fixed_ok,
       COUNT(*)
FROM dbevals
WHERE  run_id = 4
AND   fixed_confidence >=  $confidence
GROUP BY 1,
         2
""".trimIndent()
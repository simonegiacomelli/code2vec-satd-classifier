package satd.fix

import satd.step2.DbPgsql
import satd.step2.query

fun main() {
    val p = DbPgsql(hostname = "10.1.1.120")
    val c = p.connection()
    c.query(
        "select done, count(*) from dbrepos " +
                "group by done"
    ).print()
}

private fun Sequence<Array<Any>>.print() = forEach { println(it.joinToString("\t")) }


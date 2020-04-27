package satd.fix

import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import satd.step2.assert2
import satd.step2.persistence
import java.io.File
import java.sql.ResultSet


fun main() {
    val urls = File("data/backup/partial.txt")
        .readLines()
        .map {
            val res = it.substring(5).substringBefore(" ")
            println(res)
            res
        }.toSet()
    println(urls.size)
    urls.forEach {
        assert2(it.startsWith("https://github.com/"))
    }
    val list = urls.joinToString(",") { "'$it'" }
    val sql = "select min(id) , max(id) , min(created_at),max(created_at) from dbrepos where url in ($list)"
    persistence.setupDatabase()

    fun query(sql: String) = TransactionManager.current().connection.prepareStatement(sql).executeQuery().toSequence()

    transaction {
        val mm = query(sql).first()
        val min = (mm[0] as Number).toInt()
        val max = (mm[1] as Number).toInt()
        val minC = mm[2].toString()
        val maxC = mm[3].toString()

        println("min=$min max=$max diff=${max - min} minC=$minC maxC=$maxC")

        val dbUrls = query("select url from dbrepos where url in ($list)")
            .toList()
            .map { it[0].toString() }
            .toSet()
        println("Url count from db ${dbUrls.size} ")
        val missing = urls.subtract(dbUrls)
        println("Missing ${missing.size}")
//        dbUrls.forEach {
//            println(it)
//        }
    }
}

private fun ResultSet.toSequence(): Sequence<Array<Any>> = sequence {
    while (next()) {
        yield((1..metaData.columnCount).map { getObject(it) }.toTypedArray())
    }
}

package satd.step2

import com.github.javaparser.JavaParser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.transactions.transaction
import satd.step2.perf.Dataset
import satd.utils.logln
import satd.utils.loglnStart

enum class types {
    training, validation, test
}

fun assert2(value: Boolean, msg: String) {
    assert2(value) { msg }
}

fun assert2(value: Boolean, lazyMessage: () -> Any = {}) {
    if (!value) {
        val message = lazyMessage()
        throw AssertionError(message)
    }
}

val where1 by lazy {
    DbSatds.run {
        (accept.eq(1)
                and parent_count.eq(1)
                and new_clean_len.less(50)
                and old_clean_len.less(50))
    }
}

val where2 by lazy {
    DbSatds.run {
        (parent_count.eq(1)
                and old_clean_token_count.less(100)
                and new_clean_token_count.less(100))
    }
}
val where3 by lazy {
    DbSatds.run {
        (parent_count.eq(1)
                and old_clean_token_count.less(100)
                and new_clean_token_count.less(100)
                and valid.eq(1))
    }
}
val where4 by lazy {
    val urls = DbRepos.run { slice(url).select { issues.greater(100) }.map { it[url] } }
    println("repo count ${urls.size}")
    DbSatds.run {
        (parent_count.eq(1)
                and new_clean_len.less(100)
                and old_clean_len.less(100)
                and valid.eq(1)
                and url.inList(urls)
                )
    }
}

object MainGenDataset1 {
    @JvmStatic
    fun main(args: Array<String>) = generate { where1 }
}

object MainGenDataset2 {
    @JvmStatic
    fun main(args: Array<String>) = generate { where2 }
}

object MainGenDataset3 {
    @JvmStatic
    fun main(args: Array<String>) = generate { where3 }
}

object MainGenDataset4 {
    @JvmStatic
    fun main(args: Array<String>) = generate { where4 }
}

private fun generate(where: () -> Op<Boolean>) {

    val workFolder = Dataset().folder

    class Partitions(val count: Int, val train: Double, val test: Double) {

        val trainCount = (count * train).toInt()
        val testCount = (count * test).toInt()
        val validationCount = (count - (trainCount + testCount))

        val seq = genSeq().shuffled()

        private fun genSeq(): List<types> {

            assert2(train + test <= 1.0)

            return List(trainCount) { types.training } +
                    List(validationCount) { types.validation } +
                    List(testCount) { types.test }
        }

        fun type(index: Int) = seq[index]
        fun print() {
            println("Dataset $validationCount + $testCount + $trainCount  = $count (validation + test + train = total)")
        }

        fun print2() {
            println("Files ${validationCount * 2} + ${testCount * 2} + ${trainCount * 2} = ${count * 2} (validation + test + train = total)")
        }
    }

    class Main {

        fun go() {
            loglnStart("GenDataset")
            logln("Using workFolder: $workFolder")
            workFolder.deleteRecursively()
            assert2(!workFolder.exists())

            workFolder.mkdirs()
            persistence.setupDatabase()

            val typeIndexes = mutableMapOf<String, Int>()
            val ds = Partitions(transaction { queryOrdered().count() }, 0.7, 0.15)
            ds.print()
            ds.print2()
            transaction {
                queryOrdered().forEachIndexed() { idx, it ->
                    val type = ds.type(idx).toString()
                    val index = typeIndexes.getOrDefault(type, 0) + 2
                    typeIndexes[type] = index
                    writeSource(it[DbSatds.old_clean], it, "satd", type, index - 1)
                    writeSource(it[DbSatds.new_clean], it, "fixed", type, index)
                }
            }
        }

        private fun queryOrdered(): Query =
            DbSatds.select {
                where()
            }.orderBy(DbSatds.id)

        private fun writeSource(methodSource: String, it: ResultRow, type: String, subfolder: String, index: Int) {
            val folder = workFolder.resolve(subfolder)
            folder.mkdirs()
            val filename = "${index.toString().padStart(6, '0')}_${it[DbSatds.id].toString()
                .padStart(6, '0')}_${it[DbSatds.code_hash]}_$type.java"
            val content = wrapMethod(methodSource)
            try {
                val cu = JavaParser().parse(content)!!
                if (!cu.result.isPresent)
                    throw Exception("parse did not yield expected result")

                val methods = cu.result.get().types.filterNotNull().flatMap { it.methods }.filterNotNull()
                assert2(methods.size == 1)
                val method = methods.first()
                method.name.identifier = type

                folder.resolve(filename).writeText(wrapMethod(method.toString()))
            } catch (ex: Exception) {
                println("$filename\n$content")
                throw ex
            }
        }
    }

    Main().go()
}


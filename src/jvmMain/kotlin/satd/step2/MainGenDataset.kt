package satd.step2

import com.github.javaparser.JavaParser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Folders
import satd.utils.loglnStart

enum class types {
    training, validation, test
}

object MainGenDataset1 {
    @JvmStatic
    fun main(args: Array<String>) {
        generate(
            (DbSatds.accept.eq(1)
                    and DbSatds.parent_count.eq(1)
                    and DbSatds.new_clean_len.less(50)
                    and DbSatds.old_clean_len.less(50))
        )
    }
}

object MainGenDataset2 {
    @JvmStatic
    fun main(args: Array<String>) {
        generate(
            DbSatds.run {
                (parent_count.eq(1)
                        and old_clean_token_count.less(100)
                        and new_clean_token_count.less(100))
            })
    }
}

private fun generate(where: Op<Boolean>) {
    loglnStart("gen-dataset")
    class Dataset(val count: Int, val train: Double, val test: Double) {

        val trainCount = (count * train).toInt()
        val testCount = (count * test).toInt()
        val validationCount = (count - (trainCount + testCount))

        val seq = genSeq().shuffled()

        private fun genSeq(): List<types> {

            assert(train + test <= 1.0)

            return List(trainCount) { types.training } +
                    List(validationCount) { types.validation } +
                    List(testCount) { types.test }
        }

        fun type(index: Int) = seq[index]
        fun print() {
            println("Dataset $trainCount + $validationCount + $testCount = $count (train + validation + test = total)")
        }
    }

    class Main {


        val workFolder = Folders.dataset.resolve("doc2vec").toFile()

        fun go() {
            workFolder.deleteRecursively()
            assert(!workFolder.exists())

            workFolder.mkdirs()
            persistence.setupDatabase()

            val ds = Dataset(transaction { query().count() }, 0.7, 0.15)
            ds.print()
            transaction {
                query().forEachIndexed() { idx, it ->
                    val type = ds.type(idx).toString()
                    writeSource(it[DbSatds.old_clean], it, "satd", type)
                    writeSource(it[DbSatds.new_clean], it, "fixed", type)
                }
            }
        }

        private fun query(): Query =
            DbSatds.select {
                where
            }.orderBy(DbSatds.id)

        private fun writeSource(methodSource: String, it: ResultRow, type: String, subfolder: String) {
            val folder = workFolder.resolve(subfolder)
            folder.mkdirs()
            val filename = "${it[DbSatds.code_hash]}_${it[DbSatds.id]}_$type.java"
            val content = wrapMethod(methodSource)
            try {
                val cu = JavaParser().parse(content)!!
                if (!cu.result.isPresent)
                    throw Exception("parse did not yield expected result")

                val methods = cu.result.get().types.filterNotNull().flatMap { it.methods }.filterNotNull()
                assert(methods.size == 1)
                val method = methods.first()
                method.name.identifier = type

                folder.resolve(filename).writeText(wrapMethod(method.toString()))
            } catch (ex: Exception) {
                println("$filename\n$content")
            }
        }
    }

    Main().go()
}


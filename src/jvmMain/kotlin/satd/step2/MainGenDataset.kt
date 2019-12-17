package satd.step2

import com.github.javaparser.JavaParser
import org.eclipse.jgit.api.Git
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Folders
import satd.utils.dateTimeToStr
import satd.utils.loglnStart
import java.nio.file.Paths

fun main() {
    loglnStart("gen-dataset")
    class Main {


        val pers = persistence
        val workFolder = Folders.doc2vec.resolve("dataset").toFile()

        fun go() {
            workFolder.deleteRecursively()
            assert(!workFolder.exists())

            workFolder.mkdirs()
            pers.setupDatabase()
            foreachRow {
                writeSource(it[DbSatds.old_clean], it, "satd")
                writeSource(it[DbSatds.new_clean], it, "fixed")
            }
        }

        private fun writeSource(methodSource: String, it: ResultRow, type: String) {
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

                workFolder.resolve(filename).writeText(wrapMethod(method.toString()))
            } catch (ex: Exception) {
                println("$filename\n$content")
//                throw ex
            }
        }

        private fun wrapMethod(methodSource: String): String {
            val content =
                """
public class Wrapper{
${methodSource.prependIndent()}
}
""".trimIndent()
            return content
        }


        private fun foreachRow(function: (ResultRow) -> Unit) {
            transaction {
                DbSatds
                    .select {
                        DbSatds.accept.eq(1) and DbSatds.parent_count.eq(1)
                    }
                    .forEach {
                        function(it)
                    }
            }
        }

    }

    Main().go()
}


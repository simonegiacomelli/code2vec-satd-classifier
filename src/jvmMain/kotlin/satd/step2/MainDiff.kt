package satd.step2

import org.eclipse.jgit.api.Git
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Folders
import satd.utils.dateTimeToStr

fun main() {
    class Main {
        fun ResultRow.filename(): String {
            val it = this
            return "${it[DbSatds.id].toString().padStart(6, '0')}_${it[DbSatds.commit]}_${it[DbSatds.code_hash]}.java"
        }

        val ResultRow.header: String
            get() {
                val it = this
                return "pattern: ${it[DbSatds.pattern]}\n" +
                        "repo: ${it[DbSatds.repo]}\n" +
                        "commit message: ${it[DbSatds.commit_message]}\n" +
                        "\n"
            }


        fun go() {
            val folder = Folders.diff.resolve(dateTimeToStr()).toFile()
            folder.mkdir()

            persistence.setupDatabase()

//            if (true)
//                return;
            val git = Git.init().setDirectory(folder).call()!!

            foreachRow {
                val content =
                    "${it.header}${it[DbSatds.old]}"
                folder.resolve(it.filename()).writeText(content)
            }

            git.add().addFilepattern(".").call()
            git.commit().setMessage("files with satd").call()

            foreachRow {
                val content = "${it.header}${it[DbSatds.new]}"
                folder.resolve(it.filename()).writeText(content)
            }

            git.commit().setMessage("files with satd").call()
            git.add().addFilepattern(".").call()

            Runtime.getRuntime().exec(
                "diff2html -s side -- -U1000000 -M HEAD~1"
                , null
                , folder
            )
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
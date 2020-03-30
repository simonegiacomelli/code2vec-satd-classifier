package satd.step2

import org.eclipse.jgit.api.Git
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Folders
import satd.utils.dateTimeToStr
import satd.utils.logln
import satd.utils.loglnStart

object MainDiff1 {
    @JvmStatic
    fun main(args: Array<String>) {
        diff(where1)
    }
}

object MainDiff2 {
    @JvmStatic
    fun main(args: Array<String>) {
        diff(where2)
    }
}

private fun diff(where: Op<Boolean>) {
    loglnStart("diff")
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

        //        val pers = Persistence(Paths.get("./data_saved/database/foo-03-december/h2satd"))
        val pers = persistence
        val workFolder = Folders.diff.resolve(dateTimeToStr()).toFile()
        val repoFolder = workFolder.resolve("repo")
        val folderStr: String
            get() {
                val r = workFolder.toString()
                return r
            }
        val git by lazy { Git.init().setDirectory(repoFolder).call()!! }

        fun go() {

            repoFolder.mkdirs()
            pers.setupDatabase()

            diff2html()

            pers.connection().apply {
                sql.split("\nGO\n")
                    .forEach {
                        println("executing [$it]")
                        createStatement().apply {
                            execute(it)
                            close()
                        }
                    }

            }
            //pers.startWebServer()
        }

        private fun diff2html() {
            logln("workFolder: $workFolder")
            foreachRow {
                val content =
                    "${it.header}${it[DbSatds.old]}"
                repoFolder.resolve(it.filename()).writeText(content)
            }

            git.add().addFilepattern(".").call()
            git.commit().setMessage("files with satd").call()

            foreachRow {
                val content = "${it.header}${it[DbSatds.new]}"
                repoFolder.resolve(it.filename()).writeText(content)
            }

            git.commit().setMessage("files with satd").call()
            git.add().addFilepattern(".").call()

            val cmd = "diff2html --file ../diff.html  -s side -- -U1000000 -M HEAD~1"
            logln("Executing [$cmd]")
            val res = Runtime.getRuntime().exec(
                cmd
                , null
                , repoFolder
            ).waitFor()
            logln("Exit status: $res")
        }

        private fun foreachRow(function: (ResultRow) -> Unit) {
            transaction {
                DbSatds
                    .select {

                        where
                    }
                    .forEach {

                        function(it)
                    }
            }
        }


        val sql = """
call csvwrite('$folderStr/dbsatds.csv', '

SELECT * FROM DBSATDS 
where 
  accept = 1 and parent_count = 1
order by id desc

')

GO

DROP TABLE SATD_BY_pattern if exists

GO

create table satd_by_pattern as
select pattern, count(*) as pattern_count FROM DBSATDS 
where accept = 1 and parent_count = 1
group by pattern
order by count(*) desc

GO

call csvwrite('$folderStr/satd_by_pattern.csv'
,'select * from satd_by_pattern')

"""
    }

    Main().go()
}


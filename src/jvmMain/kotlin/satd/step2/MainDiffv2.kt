package satd.step2

import org.eclipse.jgit.api.Git
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Folders
import satd.utils.dateTimeToStr
import satd.utils.logln
import satd.utils.loglnStart
import java.util.concurrent.TimeUnit


object MainDiffv2OnRun {
    @JvmStatic
    fun main(args: Array<String>) = diff {
        val satd_ids = DbEvals.run {
            slice(satd_id).select { run_id.eq(1) }
                .map { it[satd_id] }
        }
        DbSatds.run { id.inList(satd_ids) }
    }
}

private fun diff(where: () -> Op<Boolean>) {
    loglnStart("diff")
    val pers = persistence
    val workFolder = Folders.diff.resolve(dateTimeToStr()).toFile()

    fun diff2html(it: ResultRow) {
        val satdIdStr = it[DbSatds.id].toString().padStart(6, '0')
        val commit = it[DbSatds.commit]
        val repoFolder = workFolder.resolve("repo/$satdIdStr")
        repoFolder.mkdirs()
        logln("satd_id ${it[DbSatds.id]}folder for diff: $repoFolder")
        val git by lazy { Git.init().setDirectory(repoFolder).call()!! }
        val header = "pattern: ${it[DbSatds.pattern]}\n" +
                "repo: ${it[DbSatds.repo]}\n" +
                "commit message: ${it[DbSatds.commit_message]}\n" +
                "\n"
        val file = repoFolder.resolve(
            "${satdIdStr}_${it[DbSatds.commit]}_${it[DbSatds.code_hash]}.java"
        )
        file.writeText("${header}${it[DbSatds.old]}")

        git.add().addFilepattern(".").call()
        git.commit().setMessage("files with satd").call()

        file.writeText("${header}${it[DbSatds.new]}")

        git.commit().setMessage("files fixed").call()
        git.add().addFilepattern(".").call()

        val cmd = "diff2html --file ../../$satdIdStr.html -o stdout -s side -- -U1000000 -M HEAD~1"
        logln("Executing [$cmd]")
        val process = ProcessBuilder(*cmd.split(" ").toTypedArray())
            .directory(repoFolder)
            .inheritIO()
            .start()
        val end = process.waitFor(1, TimeUnit.HOURS)
        val res = process.exitValue()
        assert2(end)
        assert2(res == 0)
    }

    workFolder.mkdirs()
    pers.setupDatabase()
    transaction {
        DbSatds
            .select { where() }
            .orderBy(DbSatds.id)
            .toList()
            .also { logln("Satd count ${it.size}") }
            .forEach { diff2html(it) }
    }
    logln("workFolder: $workFolder")
}


package satd.utils

import org.eclipse.jgit.api.Git

object RepoStatsFile {
    val file = Folders.database.resolve("repo-stats.csv").toFile()

    fun reset() {
        file.parentFile.mkdirs()
        file.delete()
        assert(!file.exists())
    }

    @Synchronized
    fun append(urlstr: String, rs: RepoStats) {
        rs.run { file.appendText("$urlstr,$commitCount,$sizeMB\n") }
    }

}

data class RepoStats(val commitCount: Int, val sizeMB: Long)

fun Git.stats(): RepoStats {
    val commitCount = this.log().all().call().count()
    val sizeBytes = pathSize(this.repository.workTree.toPath())

    return RepoStats(
        commitCount = commitCount,
        sizeMB = sizeBytes / (1024 * 1024)
    )
}

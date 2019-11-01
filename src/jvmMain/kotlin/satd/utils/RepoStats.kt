package satd.utils

import org.eclipse.jgit.api.Git


fun Git.printStats() {
    println("${stats()}")
}

data class RepoStats(val path: String, val commitCount: Int, val sizeMB: Long)

fun Git.stats(): RepoStats {
    val commitCount = this.log().all().call().count()
    val sizeBytes = pathSize(this.repository.workTree.toPath())

    return RepoStats(
        path = this.repository.workTree.toPath().normalize().toAbsolutePath().toString(),
        commitCount = commitCount,
        sizeMB = sizeBytes / (1024 * 1024)
    )
}

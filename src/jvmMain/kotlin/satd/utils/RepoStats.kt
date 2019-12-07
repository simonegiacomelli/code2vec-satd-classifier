package satd.utils

import org.eclipse.jgit.api.Git

data class RepoStats(val commitCount: Int, val sizeMB: Long)

fun Git.stats(): RepoStats {
    val commitCount = this.log().all().call().count()
    val sizeBytes = pathSize(this.repository.workTree.toPath())

    return RepoStats(
        commitCount = commitCount,
        sizeMB = sizeBytes / (1024 * 1024)
    )
}

package sample.jgit

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import satd.utils.Repo
import satd.utils.RepoList


class RepoCommitsHarness {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val repo = Repo(RepoList.tenRepos.first())
            if (!repo.folder.exists())
                repo.clone()

            val repository = FileRepository(repo.folder.resolve(".git"))

            Git(repository).use { git ->
                val commits = git.log().all().call()
                var count = 0
                for (commit in commits) {
                    println("LogCommit: $commit")
                    count++
                }
                println(count)
            }
        }
    }
}
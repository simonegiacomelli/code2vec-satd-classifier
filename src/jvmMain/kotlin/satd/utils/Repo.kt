package satd.utils

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import satd.step2.DbRepos
import java.io.File
import java.lang.IllegalStateException
import java.net.URL

class Repo(val urlstr: String) {
    val url = URL(urlstr)
    private val parts = url.path.drop(1).split('/')
    val userName = parts[0]
    val repoName = parts[1]
    val friendlyName = "${userName}_$repoName"
    private val reposPath get() = Folders.repos

    val textProgressMonitor = TextProgressMonitor(url.toString())
    val folder = File("$reposPath/$userName/$repoName")
    var exception: Exception? = null
    val failed get() = exception != null
    fun newGit() = open(folder)

    fun clone(): Repo {
        try {
            cloneInternal()
        } catch (ex: Exception) {
            exception = ex
        }
        return this
    }

    private fun cloneInternal() {
        if (folder.exists()) {
            if (config.if_repo_exists_check_integrity.toIntOrNull() ?: 0 == 0)
                return
            logln("$urlstr ALREADY EXISTS checking integrity")
            if (!repoOk()) {
                logln("$urlstr INTEGRITY CHECK failed. removing...")
                folder.deleteRecursively()
                if (folder.exists()) throw IllegalStateException("folder.deleteRecursively() $folder")
            }
        }

        if (!folder.exists()) {
            logln("$urlstr CLONING")
            Git.cloneRepository()
                .setNoCheckout(true)
                .setURI(url.toExternalForm())
                .setDirectory(folder)
                //                .setProgressMonitor(textProgressMonitor)
                .call()
            logln("$urlstr CLONING DONE")
        }
    }

    private fun repoOk(): Boolean {
        try {
            newGit().use { git ->
                git.clean()
                    .setCleanDirectories(true)
                    .setForce(true)
                    .call()

//                git.reset()
//                    .setMode(ResetCommand.ResetType.HARD)
//                    .setProgressMonitor(textProgressMonitor)
//                    .call()

                git.fetch()
                    .setProgressMonitor(textProgressMonitor)
                    .call()
            }
            return true
        } catch (ex: Exception) {
            logln("$urlstr REFRESH failed [${ex}]")
            return false
        }
    }

    fun open(repoFolder: File): Git {
        val git = Git.open(repoFolder)!!
        return git
    }

    fun reportFailed(): Repo {
        exception.also {
            if (it != null) DbRepos.failed(urlstr, it, "Repo.clone()")
        }
        return this
    }


}

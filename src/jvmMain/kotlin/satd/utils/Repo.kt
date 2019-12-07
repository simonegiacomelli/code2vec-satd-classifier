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
    val failedClones = Folders.log.resolve("failed_clones")
    var exception: Exception? = null
    val failed get() = exception != null
    fun newGit() = open(folder)

    fun clone(): Repo {
        try {
            cloneInternal()
        } catch (ex: Exception) {
            exception = ex
            failedClones.toFile().mkdirs()
            DbRepos.failed(urlstr, ex, "Repo.clone()")
        }
        return this
    }

    private fun cloneInternal() {


        if (folder.exists()) {
            logln("$urlstr ALREAD EXISTS checking integrity")
            if (!repoOk(folder)) {
                logln("$urlstr INTEGRITY CHECK failed. removing...")
                folder.deleteRecursively()
                if (folder.exists()) {
                    DbRepos.failed(
                        urlstr,
                        IllegalStateException("$folder"),
                        "Repo.clone() -  folder.deleteRecursively()"
                    )
                    return
                }
            }
        }

        if (!folder.exists()) {
            logln("$urlstr CLONING")
            Git.cloneRepository()
                .setURI(url.toExternalForm())
                .setDirectory(folder)
                //                .setProgressMonitor(textProgressMonitor)
                .call()
        }
    }

    private fun repoOk(repoFolder: File): Boolean {
        try {
            val git = open(repoFolder)
            git.clean()
                .setCleanDirectories(true)
                .setForce(true)
                .call()



            git.reset()
                .setMode(ResetCommand.ResetType.HARD)
                .setProgressMonitor(textProgressMonitor)
                .call()

            git.pull()
                .setProgressMonitor(textProgressMonitor)
                .call()


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


}

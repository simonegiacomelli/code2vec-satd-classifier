package satd.utils

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import java.io.File
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
            failedClones.resolve("$friendlyName.txt")
                .toFile()
                .writeText("$url\n$ex")
        }
        return this
    }

    private fun cloneInternal() {
        logln("Cloning ${url}")

        if (folder.exists()) {
            if (!repoOk(folder))
                folder.deleteRecursively()
        }

        if (!folder.exists())
            Git.cloneRepository()
                .setURI(url.toExternalForm())
                .setDirectory(folder)
//                .setProgressMonitor(textProgressMonitor)
                .call()
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
            logln("exception on ${repoFolder} ${ex}")
            return false
        }
    }

    fun open(repoFolder: File): Git {
        val git = Git.open(repoFolder)!!
        return git
    }


}

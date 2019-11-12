package satd.utils

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import satd.step1.Folders
import satd.step1.TextProgressMonitor
import java.io.File
import java.net.URL

class Repo(val url: URL) {

    private val reposPath get() = Folders.repos
    val friendlyName = url.file.drop(1).replace('/', '_')
    val textProgressMonitor = TextProgressMonitor(url.toString())
    val folder = File("$reposPath/$friendlyName")
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

package satd.step1

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import java.io.File
import java.net.URL

class Repo(val it: URL) {

    val reposPath get() = Folders.repos
    val friendlyName = it.file.drop(1).replace('/', '_')

    fun clone(): Repo {
        logln("Cloning ${it}")

        val folder = File("$reposPath/$friendlyName")

        if (folder.exists()) {
            if (!repoOk(it, folder))
                folder.deleteRecursively()
        }

        if (!folder.exists())
            Git.cloneRepository()
                .setURI(it.toExternalForm())
                .setDirectory(folder)
//                .setProgressMonitor( TextProgressMonitor( PrintWriter(System.out)))
                .call()
        return this;
    }

    private fun repoOk(repoUrl: URL, repoFolder: File): Boolean {
        try {
            val repo = Git.open(repoFolder)
            repo.clean()
                .setCleanDirectories(true)
                .setForce(true)
                .call()

            val textProgressMonitor = TextProgressMonitor(repoUrl.toString())

            repo.reset()
                .setMode(ResetCommand.ResetType.HARD)
                .setProgressMonitor(textProgressMonitor)
                .call()

            repo.pull()
                .setProgressMonitor(textProgressMonitor)
                .call()


            return true
        } catch (ex: Exception) {
            logln("exception on ${repoFolder} ${ex}")
            return false
        }
    }


}

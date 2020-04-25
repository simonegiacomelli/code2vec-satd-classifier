package satd.utils

import org.eclipse.jgit.api.Git
import satd.step2.DbRepos
import satd.step2.Exceptions
import satd.step2.repoRate
import java.io.File
import java.net.URL
import java.nio.file.Path


class Repo(val urlstr: String, private val reposPath: Path = Folders.repos) {
    val url = URL(urlstr)
    private val parts = url.path.drop(1).split('/')
    val userName = parts[0]
    val repoName = parts[1]
    val friendlyName = "${userName}_$repoName"


    val textProgressMonitor = TextProgressMonitor(url.toString())
    val folder = File("$reposPath/$userName/$repoName")
    val integrityMarker = File("$reposPath/$userName/$repoName.repo-ok.txt")
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
        if (folder.exists() && integrityMarker.exists()) {
            logln("$urlstr EXISTS and marked ok")
            return
        }
        if (folder.exists()) {
            logln("$urlstr ALREADY EXISTS checking integrity")
            if (repoOk()) {
                integrityMarker.writeText("")
                return
            }
            logln("$urlstr INTEGRITY CHECK failed. removing...")
            folder.deleteRecursively()
            if (folder.exists()) throw IllegalStateException("folder.deleteRecursively() $folder")

        }

        logln("$urlstr CLONING")
        Git.cloneRepository()
            .setNoCheckout(true)
            .setURI(url.toExternalForm())
            .setDirectory(folder)
//            .setProgressMonitor(textProgressMonitor)
            .call()
        integrityMarker.writeText("")
        logln("$urlstr CLONING DONE")
    }

    private fun repoOk(): Boolean {
        try {
            newGit().use { git ->
                git.clean()
                    .setCleanDirectories(true)
                    .setForce(true)
                    .call()

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

    fun removeCheckout() {
        if (!folder.exists())
            return

        var count = 0;
        folder.listFiles()
            .filter { it.name != ".git" }
            .forEach {
                count++;
                if (it.isFile)
                    assert(it.delete())
                else
                    assert(it.deleteRecursively())
            }
        logln("removed $count entries for $urlstr")
    }

    fun stat() {
        try {
            if (integrityMarker.exists()) {
                newGit().use { RepoStatsFile.append(urlstr, it.stats()) }
            }
        } catch (ex: Exception) {
            logln("exception $urls ${ex.javaClass.name} ${ex.message}")
        }
    }


}

package satd.utils

import org.eclipse.jgit.api.Git
import satd.step2.Cache
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
    val userFolder = File("$reposPath/$userName/")
    val folder = userFolder.resolve(repoName)
    val integrityMarker = userFolder.resolve("$repoName.repo-ok.txt")
    var exception: Exception? = null
    val toScan get() = exception == null && !knowsNoSatd

    private var knowsNoSatd = false
    fun newGit() = open(folder)
    val cache = Cache(Folders.cache.resolve("repos/${userName}/${repoName}.txt").toFile())

    fun clone(): Repo {
        try {
            knowsNoSatd = cache.knowsNoSatd()
            if (knowsNoSatd) {
                logln("$urlstr knowsNoSatd() Marking done and deleting repo")
                DbRepos.done(urlstr, "cache.knowsNoSatd")
                delete()
            } else
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
            delete()
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

    fun delete() {
        folder.deleteRecursively()
        integrityMarker.delete()
        //remove empty username folder
        if (userFolder.list().isEmpty())
            userFolder.deleteRecursively()

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

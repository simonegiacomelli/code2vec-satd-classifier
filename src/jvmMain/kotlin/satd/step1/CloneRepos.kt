package satd.step1

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.TextProgressMonitor
import java.io.File
import java.io.PrintWriter
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.ForkJoinPool

fun main() {
    CloneRepos().go()

}

class CloneRepos {
    fun logln(line: String) = println(line)
    val reposPath get() = Paths.get("./data/repos/")

    fun go() {
        val threadCount = Runtime.getRuntime().availableProcessors()
        logln("Starting ${threadCount} threads")
        val customThreadPool = ForkJoinPool(threadCount)
        customThreadPool.submit {
            repoList()
                .readText()
                .split('\n')
                .map { it.trim() }
                .filter { !it.startsWith("#") }
                .map { URL(it) }
                .parallelStream()
                .forEach { ensureRepo(it) }
        }.get()
        logln("")
        logln("Clone done")
    }

    //TODO should be a chain of commands starting from a repo url, repo in folder, repo processing, etc etc
    private fun ensureRepo(it: URL) {
        logln("Cloning ${it}")
        val folder = File(reposPath.toString() + "/" + it.file.drop(1).replace('/', '_'))

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
    }

    private fun repoOk(repoUrl: URL, repoFolder: File): Boolean {
        try {
            val repo = Git.open(repoFolder)
            repo.clean()
                .setCleanDirectories(true)
                .setForce(true)
                .call()

            repo.reset()
                .setMode(ResetCommand.ResetType.HARD)
                .call()

            repo.pull()


            return true
        } catch (ex: Exception) {
            logln("exception on ${repoFolder} ${ex}")
            return false
        }
    }

    private fun repoList() = this::class.java.classLoader.getResource("satd/step1/repo-urls.txt")!!

}

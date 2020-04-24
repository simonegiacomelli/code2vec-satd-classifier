package satd.step2

import satd.utils.*
import java.io.File
import kotlin.system.measureTimeMillis

private val tmpFolder = File("data/tmp/git-stat")

fun main() {
    loglnStart("clone")
    config.load()
//    HeapDumper.enable()

    val take = RepoList.getGithubUrls().take(30)

    logln("going to clone the following repos")
    val osCommand = take.map {
        "git clone --no-checkout $it"
    }.map {
        logln(it)
        it
    }.joinToString(" && ")

    logln(osCommand)

    logln("removed previous folders")
    logln("starting clone")


    val (jgit, git) = (1..20).map {
        Pair(runJgit(take), runOs(osCommand))
            .apply {
                logln("jgit=$first git=$second")
            }
    }.unzip()
    logln("")
    logln("statistics")
    logln("jgit=${jgit.average()}")
    logln("git =${git.average()}")
    logln("Done cloning")

}

fun runOs(sysCommand: String): Long {
    val folder = tmpFolder.resolve("git").apply {
        deleteRecursively()
        mkdirs()
    }
    val f = folder.resolve("test-git.sh")
    f.writeText(sysCommand)
    return measureTimeMillis {
        val processBuilder = ProcessBuilder("bash", f.absolutePath)
        val process = processBuilder
            .directory(folder)
            .inheritIO()
            .start()
        val x = process.waitFor()
        assert2(x == 0)
    }.apply {
        folder.deleteRecursively()
    }
}

private fun runJgit(take: List<String>): Long {
    val folder = tmpFolder.resolve("jgit").apply {
        deleteRecursively()
        mkdirs()
    }

    return measureTimeMillis {
        take.forEach { Repo(it, reposPath = folder.toPath()).clone() }
    }.apply {
        folder.deleteRecursively()
    }
}


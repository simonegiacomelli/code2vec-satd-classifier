package satd.step1

import satd.utils.Folders
import satd.utils.Repo
import satd.utils.logln
import java.nio.file.Files
import kotlin.streams.toList


class Inspec(val repo: Repo) {

    fun javaSources(): RepoSatd {
        val list = Files.walk(repo.folder.toPath())
            .parallel()
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().endsWith(".java") }
            .map { Source(it, repo).apply { filterSatd() } }
            .toList()

        val filtered = list
            .filter { it.satd.size > 0 }
            .toList()
        logln("${repo.url} source = ${list.size} filtered = ${filtered.size}")
        return RepoSatd(source = filtered, repo = repo)
    }

}

class RepoSatd(val repo: Repo, val source: List<Source>) {
    fun satdToFile() {
        Folders.satd.toFile().mkdirs()
        val snippetFile = Folders.satd.resolve(repo.friendlyName + ".snippet.java").toFile()
        if (snippetFile.exists())
            snippetFile.delete()



        snippetFile
            .printWriter()
            .use { pw ->
                pw.println("Repo url: ${repo.url}")
                if (source.isEmpty()) {
                    pw.println("No satd found")
                    return
                }
                source.forEach {
                    pw.println("-".repeat(200))
                    pw.println("-".repeat(5) + " ${it.path.fileName} ${it.path}")
                    pw.println(it.satdSnippets().joinToString("\n\n${"-".repeat(100)}\n"))

                    pw.println()
                    pw.println()
                }
            }

    }

}
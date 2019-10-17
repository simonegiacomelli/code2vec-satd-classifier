package satd.step1

import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.streams.toList


class Inspec(val repo: Repo) {

    fun javaSources(): RepoSatd {
        val list = Files.walk(repo.folder.toPath())
            .parallel()
            .filter { Files.isRegularFile(it) }
            .filter { it.toString().endsWith(".java") }
            .map { Source(it).apply { filterSatd() } }
            .toList()

        val filtered = list
            .filter { it.satd.size > 0 }
            .toList()
        logln("${repo.it} source = ${list.size} filtered = ${filtered.size}")
        return RepoSatd(source = filtered, repo = repo)
    }

}

class RepoSatd(val repo: Repo, val source: List<Source>) {
    fun satdToFile() {
        val folder = Folders.satd.resolve(repo.friendlyName).toFile()
        folder.deleteRecursively()
        folder.mkdirs()

        source.forEach {
            it.path.toFile().copyTo(File(folder, it.path.fileName.toString()))
        }

    }
}
package satd.step1

import java.nio.file.Files
import java.util.stream.Collectors


class Inspec(val repo: Repo) {

    fun javaSources(): RepoSatd {
        val list = Files.walk(repo.reposPath)
            .parallel()
            .filter { Files.isRegularFile(it) }
            .filter { it.endsWith(".java") }
            .map { Parser(it, repo).apply { filterSatd() } }
            .filter { it.satd.size > 0 }
            .collect(Collectors.toList())
        return RepoSatd(parser = list, repo = repo)
    }

}

class RepoSatd(val repo: Repo, val parser: MutableList<Parser>) {
    fun satdToFile() {
        val folder = Folders.satd.resolve(repo.friendlyName).toFile()
        folder.deleteRecursively()
        folder.mkdirs()

        parser.forEach {

        }

    }
}
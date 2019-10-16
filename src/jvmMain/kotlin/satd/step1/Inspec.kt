package satd.step1

import java.nio.file.Files
import java.util.stream.Collectors


class Inspec(val repo: Repo) {

    fun javaSources(): RepoSatd {
        val list = Files.walk(repo.reposPath)
            .parallel()
            .filter { Files.isRegularFile(it) }
            .filter { it.endsWith(".java") }
            .map { Parser(it).apply { filterSatd() } }
            .filter { it.satd.size > 0 }
            .map { it.apply { satdToFile() } }
            .collect(Collectors.toList())
        return RepoSatd(parser = list, repo = repo)
    }

}

data class RepoSatd(val repo: Repo, val parser: MutableList<Parser>)
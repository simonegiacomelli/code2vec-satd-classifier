package satd.step1

import java.net.MalformedURLException
import java.net.URL

//TODO should be moved to parent package
class RepoList() {

    companion object {
        val tenRepos by lazy { repoUrlList("satd/step1/repo-urls.txt") }
        val androidRepos by lazy { repoUrlList("satd/urls/android-repo-urls.txt") }


        fun repoTxtResource(resource: String): URL {

            return this::class.java.classLoader.getResource(resource)!!
        }

        fun repoUrlList(resource: String) = repoTxtResource(resource)
            .readText()
            .split('\n')
            .map { it.trim() }
            .filter { !it.startsWith("#") }
            .filter { it.isNotEmpty() }
            .map {
                try {
                    URL(it)
                } catch (ex: MalformedURLException) {
                    throw Exception("Offending url [$it]", ex)
                }
            }

        @JvmStatic
        fun main(args: Array<String>) {
            androidRepos.forEach {
                println(it)
            }
        }
    }
}
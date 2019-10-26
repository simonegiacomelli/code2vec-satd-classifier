package sample.jgit.customized

import org.eclipse.jgit.api.Git
import satd.step1.Folders

class GitLogAll {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
//        val guineaPig = gp1()
//        guineaPig.rebuild()
//        val git = guineaPig.git
            val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
            val sources = git.use { git ->
                val all = git.log().all().call().filter { it.parentCount==0 } .toList().map { it!! }
                println("Commit count ${all.size}")
            }

        }
    }
}
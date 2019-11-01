package satd.step2

import satd.utils.printStats

fun main(args: Array<String>) {
    val gp = satd_gp1()
    gp.rebuild()
    val git = gp.git
//            val git = Git.open(Folders.repos.resolve("square_retrofit").toFile())
//            val git = Git.open(Folders.repos.resolve("google_guava").toFile())
    git.printStats()
//            val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
    Collector(git.repository).collect()
}
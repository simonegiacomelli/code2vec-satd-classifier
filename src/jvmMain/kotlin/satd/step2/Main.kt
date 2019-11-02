package satd.step2

import org.eclipse.jgit.api.Git
import satd.step1.Folders
import satd.utils.printStats

fun main(args: Array<String>) {
//    val git = satd_gp1().apply { rebuild() }.git
//  val git = Git.open(Folders.repos.resolve("square_retrofit").toFile())
  val git = Git.open(Folders.repos.resolve("google_guava").toFile())
//    val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
    git.printStats()
//    val commits = Collector(git.repository).commits()
    Grapher2(git).trackSatd()
}
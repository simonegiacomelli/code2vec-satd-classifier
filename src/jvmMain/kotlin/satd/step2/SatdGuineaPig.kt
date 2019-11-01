package satd.step2

import sample.jgit.customized.gp1
import satd.utils.GuineaPig

/**
 * Create repository with satd and satd history
 */
abstract class SatdGuineaPig(val name: String) : GuineaPig("satd", name) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            listOf(
                gp1()
            ).forEach {
                it.rebuild()
            }
        }
    }

    fun load(s: String) = this::class.java.classLoader.getResource("satd/guinea_pig/$name/$s")!!.readText()
    fun addResource(folder: String, filename: String) {
        addFile(filename, load("$folder/$filename"))
    }
}


class satd_gp1 : SatdGuineaPig("satd_gp1") {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            satd_gp1().rebuild()
        }
    }

    fun addAndCommitResources(folder: String, vararg filenames: String) {
        filenames.forEach {
            addResource(folder, it)
        }
        git.commit().setMessage("Committing ${filenames.joinToString(", ")} for $folder").call()
    }

    override fun build() {
        addAndCommitResources("commit1", "Class1.java", "Class2.java")
        addAndCommitResources("commit2", "Class2.java")
        addAndCommitResources("commit3", "Class2.java")
        addAndCommitResources("commit4", "Class2.java")
        addAndCommitResources("commit5", "Class2.java")
    }


}

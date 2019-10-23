package sample.jgit.customized

import satd.step1.Folders
import org.eclipse.jgit.api.Git


object GuineaPigRepos {
    val gp1 by lazy { gp1() }
}

class gp1 {
    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            gp1().rebuild()
        }
    }
    val rootPath = Folders.guineaPigRepos.resolve("gp1")
    val root = rootPath.toFile();
    fun rebuild() {
        root.deleteRecursively()
        assert(!root.exists())
        root.mkdirs()

        git(::build)
    }

    fun git(l: (Git) -> Unit) {
        Git.init()
            .setDirectory(root)
            .call()!!.use(l)
    }

    private fun build(git: Git) {
        val foo = rootPath.resolve("foo.txt").toFile()
        foo.writeText("I'm foo")
        git.add().addFilepattern(foo.name).call()
        git.commit().setMessage("Added foo").call()

        val bar = rootPath.resolve("bar.txt").toFile()
        bar.writeText("I'm bar")
        git.add().addFilepattern(bar.name).call()
        git.commit().setMessage("Added bar").call()


    }
}
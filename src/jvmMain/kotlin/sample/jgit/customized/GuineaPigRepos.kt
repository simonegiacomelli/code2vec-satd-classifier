package sample.jgit.customized

import satd.step1.Folders
import org.eclipse.jgit.api.Git


object GuineaPigRepos {
    val gp1 by lazy { gp1() }
}

class gp1 {
    companion object {
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
        commitFile(git, "foo.txt", "I'm foo", "Added foo")
        commitFile(git, "bar.txt", "I'm bar", "Added bar")
        git.branchCreate().setName("branch1").call()
        commitFile(git, "baz.txt", "I'm baz", "Added baz into branch1")


    }

    private fun commitFile(git: Git, filename: String, content: String, commitMessage: String) {
        rootPath.resolve(filename).toFile().writeText(content)
        git.add().addFilepattern(filename).call()
        git.commit().setMessage(commitMessage).call()
    }
}
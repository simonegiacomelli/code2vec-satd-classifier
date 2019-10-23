package sample.jgit.customized

import satd.step1.Folders
import org.eclipse.jgit.api.Git


object GuineaPigRepos {
    val gp1 by lazy { gp1() }
    val gp_3_parents by lazy { gp_3_parents() }
}

class gp1 {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            gp1().rebuild()
        }
    }

    val workTree = Folders.guineaPigRepos.resolve("gp1").toFile()

    fun rebuild() {
        workTree.deleteRecursively()
        assert(!workTree.exists())
        workTree.mkdirs()
        git { Rebuild(it).build() }
    }

    fun git(l: (Git) -> Unit) = Git.init().setDirectory(workTree).call()!!.use(l)


    class Rebuild(val git: Git) {

        fun build() {
            commitFile("foo.txt", "I'm foo", "Added foo")
            commitFile("bar.txt", "I'm bar", "Added bar")
            val branch1 = "branch1"
            git.checkout().setName(branch1).setCreateBranch(true).call()
            commitFile("pluto.txt", "I'm pluto", "Added pluto")
            git.checkout().setName("master").call()
            commitFile("baz.txt", "I'm baz", "Added baz into $branch1")
            git.checkout().setName(branch1).call()
            commitFile("foo.txt", "I'm foo\nyes, you  are foo.", "Update foo.txt in $branch1")
            git.checkout().setName("master").call()
            commitFile("bar.txt", "I'm bar\nyes bar", "Update bar")
            git.merge()
                .include(git.repository.resolve(branch1))
                .setCommit(true)
                .setMessage("merge1")
                .call()

        }

        private fun commitFile(file: String, content: String, message: String) {
            git.repository.workTree.resolve(file).writeText(content)
            git.add().addFilepattern(file).call()
            git.commit().setMessage(message).call()
        }
    }

}

class gp_3_parents {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            gp_3_parents().rebuild()
        }
    }

    val workTree = Folders.guineaPigRepos.resolve("gp_3_parents").toFile()

    fun rebuild() {
        workTree.deleteRecursively()
        assert(!workTree.exists())
        workTree.mkdirs()
        git { Rebuild(it).build() }
    }

    fun git(l: (Git) -> Unit) = Git.init().setDirectory(workTree).call()!!.use(l)


    class Rebuild(val git: Git) {

        fun build() {
            commitFile("foo.txt", "I'm foo", "Added foo")
            val branch1 = "branch1"
            val branch2 = "branch2"
            git.checkout().setName(branch1).setCreateBranch(true).call()
            commitFile("bar.txt", "I'm bar", "Added bar into $branch1")
            git.checkout().setName("master").call()
            commitFile("branch-intruder.txt", "between branches", "between branches")
            git.checkout().setName(branch2).setCreateBranch(true).call()
            commitFile("baz.txt", "I'm baz", "Added baz into $branch2")
            git.checkout().setName("master").call()
            commitFile("pluto.txt", "I'm pluto", "Added pluto")

            //the following would give "Error: Your local changes to the following files would be overwritten by merge"
            //addFile("new-file.txt","in master, just before merge")

            //jgit seems not to implement octopus merge strategy
            ProcessBuilder("git merge $branch1 $branch2 -m merge1".split(' '))
                .directory(git.repository.workTree)
                .inheritIO()
                .start()
                .waitFor()

        }

        private fun commitFile(file: String, content: String, message: String) {
            addFile(file, content)
            git.commit().setMessage(message).call()
        }

        private fun addFile(file: String, content: String) {
            git.repository.workTree.resolve(file).writeText(content)
            git.add().addFilepattern(file).call()
        }
    }

}
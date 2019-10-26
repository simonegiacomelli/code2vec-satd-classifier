package sample.jgit.customized

import satd.step1.Folders
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand


abstract class guinea_pig(name: String) : AutoCloseable {
    val workTree = Folders.guineaPigRepos.resolve(name).toFile()

    fun newGit() = Git.init().setDirectory(workTree).call()!!

    private val lazyGit = lazy { newGit() }
    val git by lazyGit

    override fun close() {
        if (lazyGit.isInitialized()) git.close()
    }

    fun rebuild() {
        workTree.deleteRecursively()
        assert(!workTree.exists())
        workTree.mkdirs()
        this.use {
            build()
        }
    }

    fun commitFile(file: String, content: String, message: String) {
        addFile(file, content)
        git.commit().setMessage(message).call()
    }

    fun commitFile(file: String, content: String) {
        addFile(file, content)
        git.commit().setMessage("Adding $file").call()
    }

    fun commitFile(file: String) {
        addFile(file, "Content of $file")
        git.commit().setMessage("Adding $file").call()
    }

    fun addFile(file: String, content: String) {
        git.repository.workTree.resolve(file).writeText(content)
        git.add().addFilepattern(file).call()
    }

    protected abstract fun build()

    /**
     * jgit does not support all git features, so we fallback to git command line
     * e.g. octopus merge
     */
    fun sysGit(command: String) {
        ProcessBuilder(command.split(' '))
            .directory(git.repository.workTree)
            .inheritIO()
            .start()
            .waitFor()
    }

}

class gp1 : guinea_pig("gp1") {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            gp1().rebuild()
        }
    }

    override fun build() {
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


}

class gp_branch_with_3_parents : guinea_pig("gp_branch_with_3_parents") {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            gp_branch_with_3_parents().rebuild()
        }
    }

    override fun build() {
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

        //jgit do not to implement octopus merge strategy
        sysGit("git merge $branch1 $branch2 -m merge1")

    }
}

class gp_dangling : guinea_pig("gp_dangling") {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            gp_dangling().rebuild()
        }
    }

    override fun build() {
        commitFile("foo.txt", "I'm foo", "Added foo")
        commitFile("bar.txt", "I'm bar", "Added bar")
        git.checkout().setName("dangling1").setOrphan(true).call()
        commitFile("dangling.txt", "I'm dangling", "Added dangling")
    }
}

class gp_three_source_dag : guinea_pig("gp_three_source_dag") {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            gp_three_source_dag().rebuild()
        }
    }

    override fun build() {
        commitFile("master_x.txt")
        commitFile("master_y.txt")
        val branch1 = "dangling1"
        git.checkout().setName(branch1).setOrphan(true).call()
        git.reset().setMode(ResetCommand.ResetType.HARD).call()
        commitFile("dangling1_x.txt")
        commitFile("dangling1_y.txt")
        val branch2 = "dangling2"
        git.checkout().setName(branch2).setOrphan(true).call()
        git.reset().setMode(ResetCommand.ResetType.HARD).call()
        commitFile("dangling2_x.txt")
        commitFile("dangling2_y.txt")

        git.checkout().setName("master").call()
        git.reset().setMode(ResetCommand.ResetType.HARD).call()

        //this will complain but the merge will be performed and that fact recorded although there are no changes in the index
        //https://stackoverflow.com/a/31186732/316766
        sysGit("git merge $branch1 $branch2 -m merge1 --allow-unrelated-histories")
        sysGit("git read-tree master $branch1 $branch2")

        git.commit().setMessage("merge1").call()

        git.reset().setMode(ResetCommand.ResetType.HARD).call()

        commitFile("after_merge.txt")
    }

}

package satd.utils

import org.eclipse.jgit.api.Git

abstract class GuineaPig(group: String, name: String) : AutoCloseable {
    val workTree = Folders.guineaPigRepos.resolve(group).resolve(name).toFile()

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
        sysGit("git log --all --decorate --oneline --graph")
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
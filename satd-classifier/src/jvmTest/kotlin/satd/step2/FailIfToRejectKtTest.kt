package satd.step2

import org.eclipse.jgit.api.Git
import kotlin.test.Test
import kotlin.test.assertFails

class FailIfToRejectKtTest {

    @Test
    fun testFailIfToReject() {
        val git = git()
        git.addFile("Android.mk", "x")
        git.addFile("CleanSpec.mk", "x")
        commit(git)
        assertFails {
            failIfToReject(git)
        }
    }

    @Test
    fun `good repo should be accepted`() {
        val git = git()
        git.addFile("README.md", "x")
        commit(git)
        failIfToReject(git)
    }

    private fun commit(git: Git) {
        git.commit().setMessage("initial commit").call()
    }

    private fun git(): Git {
        val tmp = createTempDir()
        val git = Git.init().setDirectory(tmp).call()!!
        return git
    }

    private fun Git.addFile(file: String, content: String) {
        this.repository.workTree.resolve(file).writeText(content)
        this.add().addFilepattern(file).call()
    }

}
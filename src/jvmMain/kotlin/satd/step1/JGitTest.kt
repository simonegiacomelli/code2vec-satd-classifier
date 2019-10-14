package satd.step1

import org.eclipse.jgit.api.Git
import java.io.File

fun main() {
    Git.cloneRepository()
        .setURI("https://github.com/eclipse/jgit.git")
        .setDirectory(File("./data/repos"))
        .call()
}
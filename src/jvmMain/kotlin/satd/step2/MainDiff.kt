package satd.step2

import org.eclipse.jgit.api.Git
import satd.utils.Folders
import satd.utils.dateTimeToStr

fun main() {
    val folder = Folders.diff.resolve(dateTimeToStr()).toFile()
    folder.mkdir()

    val git = Git.init().setDirectory(folder).call()!!

    folder.resolve("pre.java").writeText("ciao Simone, come stai?")
    git.add().addFilepattern(".").call()
    git.commit().setMessage("files with satd").call()
    folder.resolve("pre.java").writeText("ciao Simo!")
    git.commit().setMessage("files with satd").call()
    git.add().addFilepattern(".").call()

    Runtime.getRuntime().exec(
        "diff2html -s line -f html -d word -i command -o preview -- -M HEAD~1"
        , null
        , folder
    )

}
package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import satd.utils.AntiSpin
import satd.utils.Rate
import java.nio.charset.Charset


/**
 * Creates graph of satd evolution through commits
 */
class Grapher2(val git: Git) {
    val repo = git.repository
    val blobs = mutableMapOf<ObjectId, Blob>()

    val reader = git.repository.newObjectReader()
    val emptyTreeIterator = EmptyTreeIterator()

    val edgeRate = Rate(10)
    val blobRate = Rate(10)
    val satdRate = Rate(10)
    val commitRate = Rate(10)
    val ratePrinter =
        AntiSpin { println("commit#:${commitRate.spinCount} edge#:${edgeRate.spinCount} edge/sec:$edgeRate blob/sec:$blobRate satd/sec: $satdRate") }

    fun trackSatd() {

        val walk = CRevWalk(git.repository)
        walk.all()
        for (child in walk.call()) {
            commitRate.spin()
            if (child.parents.isNotEmpty())
                child.parents.forEach {
                    val parent = it as CRevCommit
                    visitEdge(child, parent.newTreeIterator())
                }
            else
                visitEdge(child, emptyTreeIterator)

            ratePrinter.spin()
        }
        ratePrinter.callback()
    }

    fun CRevCommit.newTreeIterator() = CanonicalTreeParser().apply { reset(reader, tree) }


    private fun visitEdge(child: CRevCommit, parentTree: AbstractTreeIterator) {
        edgeRate.spin()
        //println("visiting parent --------------> $child ${child.shortMessage}")
        DiffFormatter(DisabledOutputStream.INSTANCE).use { formatter ->
            formatter.setRepository(git.repository)
            val entries = formatter.scan(parentTree, child.newTreeIterator())

            entries.forEach {
                //                val fileHeader = formatter.toFileHeader(it)
//                println("${it.changeType} " + fileHeader.toEditList())
                when (it.changeType) {
                    DiffEntry.ChangeType.ADD, DiffEntry.ChangeType.MODIFY ->
                        process(child, it.newId.toObjectId(), it.newPath)

                }
            }
            //            println("DiffEntries ${entries.size}")
        }
    }

    private fun process(commit: CRevCommit, objectId: ObjectId, filename: String) {
        if (!filename.endsWith(".java"))
            return
        ratePrinter.spin()
        val blob = blobs.getOrPut(objectId) {
            blobRate.spin()
            val content = repo.open(objectId).bytes.toString(Charset.forName("UTF-8"))
            val blob = Blob(objectId, content)
            if (blob.satdList.isNotEmpty())
                satdRate.spin()
            blob
        }

        if (blob.satdList.isNotEmpty())
            commit.addSatd(blob, filename)

    }

}


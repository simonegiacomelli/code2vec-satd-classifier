package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry.ChangeType.*
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
    val blobs = mutableMapOf<ObjectId, SourceWithId>()

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
                    visitEdge(child, parent, parent.newTreeIterator())
                }
//            else visitEdge(child, emptyTreeIterator)
            else visitEdge(child, CRevCommit(ObjectId.zeroId()), emptyTreeIterator)

            ratePrinter.spin()
        }
        ratePrinter.callback()
    }

    fun CRevCommit.newTreeIterator() = CanonicalTreeParser().apply { reset(reader, tree) }


    private fun visitEdge(child: CRevCommit, parent: CRevCommit, parentIterator: AbstractTreeIterator) {
        edgeRate.spin()
        DiffFormatter(DisabledOutputStream.INSTANCE).use { formatter ->
            formatter.setRepository(git.repository)
            val entries = formatter.scan(parentIterator, child.newTreeIterator())

            entries.forEach {
                ratePrinter.spin()
                if (it.newPath.endsWith(".java"))
                    when (it.changeType) {
                        ADD -> {
                            child.addSatd(getObjectSatdForId(it.newId.toObjectId()), it.newPath)
                        }
                        COPY, RENAME, MODIFY -> {
                            child.addSatd(getObjectSatdForId(it.newId.toObjectId()), it.newPath)
                            parent.addSatd(getObjectSatdForId(it.oldId.toObjectId()), it.oldPath)
                        }
                        DELETE -> {
                            println("delete ${it.newPath}")
                            parent.addSatd(getObjectSatdForId(it.oldId.toObjectId()), it.oldPath)
                        }
                        null -> TODO()
                    }
            }
        }
    }

    private fun getObjectSatdForId(objectId: ObjectId): SourceWithId {
        val objectSatd = blobs.getOrPut(objectId) {
            blobRate.spin()
            val content = repo.open(objectId).bytes.toString(Charset.forName("UTF-8"))
            val objectSatd = SourceWithId(objectId, content)
            if (objectSatd.satdList.isNotEmpty())
                satdRate.spin()
            objectSatd
        }
        return objectSatd
    }

}


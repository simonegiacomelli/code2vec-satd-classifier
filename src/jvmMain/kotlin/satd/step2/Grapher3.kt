package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType.*
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.AbbreviatedObjectId
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import satd.step1.Folders
import satd.utils.AntiSpin
import satd.utils.Rate
import satd.utils.printStats
import java.nio.charset.Charset


/**
 * Creates graph of satd evolution through commits
 */
class Grapher3(val git: Git) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
//            val git = satd_gp1().apply { rebuild() }.git
            val git = Git.open(Folders.repos.resolve("PhilJay_MPAndroidChart").toFile())
//            val git = Git.open(Folders.repos.resolve("square_retrofit").toFile())
//            val git = Git.open(Folders.repos.resolve("google_guava").toFile())
//            val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
            git.printStats()
//    val commits = Collector(git.repository).commits()
            val g = Grapher3(git).apply { trackSatd() }
//            DotGraph(g.allSatds, git.repository.workTree.name).full()
        }
    }

    val repo = git.repository
    val repoName get() = git.repository.workTree.name
    val allSatds = mutableMapOf<ObjectId, SourceWithId>()

    val reader = git.repository.newObjectReader()
    val emptyTreeIterator = EmptyTreeIterator()

    val edgeRate = Rate(10)
    val sourceRate = Rate(10)
    val satdRate = Rate(10)
    val commitRate = Rate(10)
    val ratePrinter =
        AntiSpin { println("commit#:${commitRate.spinCount} edge#:${edgeRate.spinCount} source#:${sourceRate.spinCount} edge/sec:$edgeRate source/sec:$sourceRate satd/sec: $satdRate") }

    val satdCache = SatdCache(repoName)

    fun trackSatd() {


        for (child in git.log().all().call()) {
            commitRate.spin()
            if (child.parents.isNotEmpty())
                child.parents.forEach {
                    val parent = it
                    visitEdge(child.newTreeIterator(), parent.newTreeIterator(), child, parent)
                }
            else visitEdge(child.newTreeIterator(), emptyTreeIterator, child, ObjectId.zeroId())

            ratePrinter.spin()
        }
        ratePrinter.callback()

    }

    private fun RevCommit.newTreeIterator() = CanonicalTreeParser().apply { reset(reader, tree) }
    private fun AbbreviatedObjectId.source() = processedSatds(this.toObjectId())
    private fun DiffEntry.isJavaSource() = this.newPath.endsWith(".java") || this.oldPath.endsWith(".java")

    private fun visitEdge(
        childIterator: AbstractTreeIterator,
        parentIterator: AbstractTreeIterator,
        childCommit: ObjectId,
        parentCommit: ObjectId
    ) {
        edgeRate.spin()
        DiffFormatter(DisabledOutputStream.INSTANCE).use { formatter ->
            formatter.setRepository(git.repository)
            val entries = formatter.scan(parentIterator, childIterator)

            entries
                .filterNotNull()
                .filter { it.isJavaSource() }
                .forEach {
                    val info = it.toInfo(parentCommit, childCommit)
                    ratePrinter.spin()
                    when (it.changeType) {
                        ADD -> it.newId.source().add(info)
                        MODIFY -> it.newId.source().modify(info, it.oldId.source())
                        DELETE -> it.oldId.source().delete(info)
                        COPY, RENAME -> {
                            /*should not matter to our satd tracking*/
                        }
                        null -> TODO()
                    }

                }
        }
    }

    private fun processedSatds(objectId: ObjectId): SourceWithId {
        val objectSatd = allSatds.getOrPut(objectId) {
            sourceRate.spin()
            val content = repo.open(objectId).bytes.toString(Charset.forName("UTF-8"))
//            val content = ""
            val objectSatd = SourceWithId(objectId, content)
            if (objectSatd.satdList.isNotEmpty())
                satdRate.spin()
            objectSatd
        }
        return objectSatd
    }

    private fun DiffEntry.toInfo(oldCommitId: AnyObjectId, newCommitId: AnyObjectId) =
        Info(
            changeType = changeType,
            oldCommitId = oldCommitId,
            newCommitId = newCommitId,
            newId = newId.toObjectId(),
            oldId = oldId.toObjectId(),
            newPath = newPath,
            oldPath = oldPath
        )

}


class DotGraph(val allSatds: MutableMap<ObjectId, SourceWithId>, val filename: String) {
    val AnyObjectId.esc get() = "\"$this\""
    val AnyObjectId.abb get() = "${this.abbreviate(7).name()}"
    val folder = Folders.satd.resolve("dots").toFile()

    fun full(): Dot {

        folder.mkdirs()
        folder.resolve("$filename.dot").bufferedWriter().use { buf ->
            buf.appendln("digraph one {")
            allSatds.values.forEach { child ->
                child.parents.forEach { parent ->
                    val label = parent.value.joinToString(", ") {
                        "${it.oldCommitId.abb} -> ${it.newCommitId.abb} "
                    }
//                    val label = ${child} -> ${parent.key}
                    buf.appendln("  ${child.esc} -> ${parent.key.esc} [ label = \"$label\"] ")
                }
                val labelList = mutableListOf(child.names
                    .map { it.substringAfterLast("/") }
                    .joinToString(", "))
                labelList.addAll(child.commits.map { it.abb })

                val label = labelList.joinToString("\\n")
                buf.appendln("  ${child.esc} [ label = \"$label\" ] ")
            }
            buf.appendln("}")
        }
        return Dot(filename)

    }

    fun satd(): Dot {
        val filename = this.filename + "_satd"
        val folder = Folders.satd.resolve("dots").toFile()
        folder.mkdirs()
        folder.resolve("$filename.dot").bufferedWriter().use { buf ->
            buf.appendln("digraph one {")
            allSatds.values.forEach { child ->
                child.parents.forEach { parent ->
                    val label = parent.value.joinToString(", ") {
                        "${it.oldCommitId.abb} -> ${it.newCommitId.abb} "
                    }
//                    val label = ${child} -> ${parent.key}
                    buf.appendln("  ${child.esc} -> ${parent.key.esc} [ label = \"$label\"] ")
                }
                val labelList = mutableListOf(child.names
                    .map { it.substringAfterLast("/") }
                    .joinToString(", "))
                labelList.addAll(child.commits.map { it.abb })

                val label = labelList.joinToString("\\n")
                buf.appendln("  ${child.esc} [ label = \"$label\" ] ")
            }
            buf.appendln("}")
        }
        return Dot(filename)

    }

    inner class Dot(val filename: String) {

        fun toPng() {
            val cmd = "dot -Tpng -o $filename.png $filename.dot"
            println("Running [$cmd]")
            Runtime.getRuntime().exec(cmd, null, folder).waitFor()
        }
    }
}

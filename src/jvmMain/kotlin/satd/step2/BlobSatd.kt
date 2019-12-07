package satd.step2

import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.logln
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.math.absoluteValue

class BlobSatd(val repo: Repository, val stat: Stat) {
    val allSatds = mutableMapOf<ObjectId, SourceInfo>()
    val repoName = repo.workTree.name
//    val cache = CacheSpin("containsSatd", repoName)

    fun processedSatds(objectId: ObjectId): SourceInfo =
        allSatds.getOrPut(objectId) {
            stat.sourceRate.spin()
            //val containsSatd = cache[objectId.name]
//            val satdMethods = if (containsSatd != "0") findMethodsWithSatd(objectId.content()) else emptyList()
//            if (containsSatd == null)
//                cache[objectId.name] = if (satdMethods.isEmpty()) "0" else "1"
            val satdMethods = findMethodsWithSatd(objectId.content())
            SourceInfo(objectId, satdMethods.map { it.name to it }.toMap().toMutableMap())
        }

    private fun ObjectId.content() = repo.open(this).bytes.toString(Charset.forName("UTF-8"))

    inner class SourceInfo(val objectId: ObjectId, val methods: MutableMap<String, Method>) {

        fun link(oldSource: SourceInfo, oldCommitId: AnyObjectId, newCommitId: RevCommit) {
            //we are interested in disappearing satd to the next state of the method
            //we are not interested in the previous state of a method with satd

            val oldSatd = oldSource.methods.values.filter { it.hasSatd }
            val oldNamesWithSatd = oldSatd.map { it.name }.toSet()
            val missingNewMethodsNames = oldNamesWithSatd.subtract(methods.keys)

            val missingNewMethods = findMethodsByName(objectId.content(), missingNewMethodsNames)
            methods.putAll(missingNewMethods.map { it.name to it })

            //now old and new contains matching methods instances
            oldSatd.forEach { old ->
                val new = methods.get(old.name)!!
                if (old.hasSatd && new.exists && !new.hasSatd)
                    candidateForDb(old, new, newCommitId)
            }
        }

        private fun candidateForDb(
            old: Method,
            new: Method,
            newCommitId: RevCommit
        ) {
            val req = Requirements(old, new)
            if (!req.accept())
                return
            transaction {
                val oldClean = "${req.oldClean}"
                val newClean = "${req.newClean}"
                val codeHashStr = "$oldClean\n------\n$newClean".sha1()
                if (DbSatds.existsCodeHash(codeHashStr))
                    logln("${stat.repo.urlstr} DUPLICATE satd skipping; it already has code hash $codeHashStr")
                else
                    DbSatds.insert {
                        it[this.repo] = repoName
                        it[this.commit] = "${newCommitId.name}"
                        it[this.old] = "${old.method}"
                        it[this.new] = "${new.method}"
                        it[this.pattern] = "${old.pattern}"
                        it[this.old_len] = "${old.method}".lines().size
                        it[this.new_len] = "${new.method}".lines().size
                        it[this.commit_message] = newCommitId.fullMessage

                        val oldCleanLen = oldClean.lines().size
                        val newCleanLen = newClean.lines().size

                        it[this.old_clean] = oldClean
                        it[this.new_clean] = newClean
                        it[this.old_clean_len] = oldCleanLen
                        it[this.new_clean_len] = newCleanLen
                        val clDiffRatio = (oldCleanLen - newCleanLen).absoluteValue.toDouble() / newCleanLen
                        it[this.clean_diff_ratio] = clDiffRatio
                        it[this.code_hash] = codeHashStr
                        val acc =
                            oldCleanLen < 50 && newCleanLen < 50 && clDiffRatio < 0.25 && newCommitId.parentCount == 1
                        it[this.accept] = if (acc) 1 else 0
                        it[this.parent_count] = newCommitId.parentCount
                    }
                stat.satdRate.spin()

            }

        }


    }

    private fun String.sha1(): String {
        val bytes = MessageDigest.getInstance("SHA-1")
            .digest(toByteArray())
        return bytes.joinToString("") { String.format("%02X", (it.toInt() and 0xFF)) }
    }
}

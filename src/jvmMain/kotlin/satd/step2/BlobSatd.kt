package satd.step2

import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Folders
import satd.utils.logln
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.math.absoluteValue
import kotlin.reflect.KFunction1

class BlobSatd(val repo: Repository, val stat: Stat) {
    val repoName = repo.workTree.name
    val cache = Cache(Folders.cache.resolve("repos/${stat.repo.userName}/${stat.repo.repoName}.txt").toFile())
    val url = stat.repo.urlstr

    private fun processedSatdsYesCache(objectId: ObjectId): SourceInfo {
        stat.sourceRate.spin()
        if (cache[objectId.name] != "1")
            return SourceInfo(objectId, mutableMapOf())

        val satdMethods = findMethodsWithSatd(objectId.content())
        return SourceInfo(objectId, satdMethods.map { it.name to it }.toMap().toMutableMap())
    }

    private fun processedSatdsNoCache(objectId: ObjectId): SourceInfo {
        stat.sourceRate.spin()
        val satdMethods = findMethodsWithSatd(objectId.content())
        if (satdMethods.isNotEmpty())
            cache[objectId.name] = "1"
        return SourceInfo(objectId, satdMethods.map { it.name to it }.toMap().toMutableMap())
    }

    fun processedSatds(objectId: ObjectId): SourceInfo = processedSatdsPriv(objectId)
    private val processedSatdsPriv = determineProcessStrategy()

    fun repoIsCompleted() {
        cache.store()
    }

    private fun determineProcessStrategy(): KFunction1<ObjectId, SourceInfo> {
        if (cache.exists()) {
            cache.load()
            return ::processedSatdsYesCache
        }

        return ::processedSatdsNoCache
    }

    private fun ObjectId.content() = repo.open(this).bytes.toString(Charset.forName("UTF-8"))

    inner class SourceInfo(val objectId: ObjectId, val methods: MutableMap<String, Method>) {

        fun link(oldSource: SourceInfo, oldCommitId: AnyObjectId, newCommitId: RevCommit) {
            //we are interested in disappearing satd to the next state of the method
            //we are not interested in the previous state of a method with satd

            val oldSatd = oldSource.methods.values.filter { it.hasSatd }

            if (oldSatd.isEmpty())
                return

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
                if (noDuplicates(codeHashStr, newCommitId))
                    DbSatds.insert {
                        it[this.repo] = repoName
                        it[this.url] = this@BlobSatd.url
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

    private fun noDuplicates(codeHashStr: String, revCommit: RevCommit): Boolean {
        val dup = DbSatds.duplicateCodeHash(codeHashStr) ?: return true
        val commit = revCommit.id.name.orEmpty()
        val descr = when {
            url == dup.url -> {
                assert2(commit != dup.commit)
                "same repository. commit $commit"
            }
            dup.commit == commit -> "(${dup.url} SAME commit id!"
            else -> "(${dup.url} commit ${dup.commit})"
        }

        logln("${stat.repo.urlstr} DUPLICATE satd already in $descr - skipping code hash $codeHashStr")
        return false
    }


    private fun String.sha1(): String {
        val bytes = MessageDigest.getInstance("SHA-1")
            .digest(toByteArray())
        return bytes.joinToString("") { String.format("%02X", (it.toInt() and 0xFF)) }
    }
}

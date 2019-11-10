package satd.step2

import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Rate
import java.nio.charset.Charset

class BlobSatd(val repo: Repository,val  stat: Stat) {
    val allSatds = mutableMapOf<ObjectId, SourceInfo>()


    val repoName = repo.workTree.name

    fun processedSatds(objectId: ObjectId): SourceInfo {
        val objectSatd = allSatds.getOrPut(objectId) {
            stat.sourceRate.spin()
            val content = objectId.content()
            val satdMethods = findMethodsWithSatd(content)
            SourceInfo(objectId, satdMethods.map { it.name to it }.toMap().toMutableMap())
        }
        return objectSatd
    }

    private fun ObjectId.content() = repo.open(this).bytes.toString(Charset.forName("UTF-8"))

    inner class SourceInfo(val objectId: ObjectId, val methods: MutableMap<String, Method>) {

        fun link(oldSource: SourceInfo, oldCommitId: AnyObjectId, newCommitId: AnyObjectId) {
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
                if (old.hasSatd && new.exists && !new.hasSatd) {
                    transaction {
                        DbSatds.insert {
                            it[this.repo] = repoName
                            it[this.commit] = "${newCommitId.name}"
                            it[this.satd] = "${old.method}"
                            it[this.fixed] = "${new.method}"
                        }
                    }
                    stat.satdRate.spin()
                }
            }
        }


    }

}
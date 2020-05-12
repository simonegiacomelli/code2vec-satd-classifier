package satd.step2

import com.github.javaparser.JavaParser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.transactions.transaction
import satd.step2.perf.Sample
import satd.utils.logln
import satd.utils.loglnStart


fun assert2(value: Boolean, msg: String) {
    assert2(value) { msg }
}

fun assert2(value: Boolean, lazyMessage: () -> Any = {}) {
    if (!value) {
        val message = lazyMessage()
        throw AssertionError(message)
    }
}

val where1 by lazy {
    DbSatds.run {
        (parent_count.eq(1)
                and new_clean_len.less(15)
                and old_clean_len.less(15)
                and valid.eq(1)
                and url.inList(urlsIssuesGreaterThan100())
                )
    }
}

val where2 by lazy {
    DbSatds.run {
        (parent_count.eq(1)
                and old_clean_token_count.less(100)
                and new_clean_token_count.less(100))
    }
}
val where3 by lazy {
    DbSatds.run {
        (parent_count.eq(1)
                and old_clean_token_count.less(900)
                and new_clean_token_count.less(900)
                and valid.eq(1))
    }
}
val where4 by lazy {
    DbSatds.run {
        (parent_count.eq(1)
                and old_clean_token_count.less(2500)
                and new_clean_token_count.less(2500)
                and valid.eq(1)
                and accept.eq(1)
                and url.inList(urlsIssuesGreaterThan100())
                )
    }
}

private fun urlsIssuesGreaterThan100(): List<String> {
    val urls = DbRepos.run {
        slice(url).select {
            issues.greater(100).and(done.eq(1)).and(success.eq(1))
        }.map { it[url] }
    }
    println("repo count ${urls.size}")
    return urls
}

object MainGenDataset1 {
    @JvmStatic
    fun main(args: Array<String>) = generate { where1 }
}

object MainGenDataset2 {
    @JvmStatic
    fun main(args: Array<String>) = generate { where2 }
}

object MainGenDataset3 {
    @JvmStatic
    fun main(args: Array<String>) = generate { where3 }
}

object MainGenDataset4 {
    @JvmStatic
    fun main(args: Array<String>) = generate { where4 }
}

fun generate(where: () -> Op<Boolean>) {

    val mainImportPredictions = MainImportPredictions()
    val workFolder = mainImportPredictions.folder
    val infoFile = mainImportPredictions.infoFile


    fun writeSource(
        satdId: Long,
        subfolder: String,
        type: String,
        methodSource: String,
        index: Int
    ) {
        val folder = workFolder.resolve(subfolder)
        folder.mkdirs()

        val filename = Sample(satdId, type, index).filename()
        val content = wrapMethod(methodSource)
        try {
            val cu = JavaParser().parse(content)!!
            if (!cu.result.isPresent) throw Exception("parse did not yield expected result")

            val methods = cu.result.get().types.filterNotNull().flatMap { it.methods }.filterNotNull()
            assert2(methods.size == 1) { "methods.size=${methods.size} content=[[$content]]" }
            val method = methods.first()
            method.name.identifier = type

            folder.resolve(filename).writeText(wrapMethod(method.toString()))
        } catch (ex: Exception) {
            println("$filename\n$content")
            throw ex
        }
    }


    loglnStart("GenDataset")
    logln("Using workFolder: $workFolder")
    workFolder.deleteRecursively()
    assert2(!workFolder.exists())

    workFolder.mkdirs()
    persistence.setupDatabase()

    fun query(): Query = DbSatds.select { where() }

    val typeIndexes = mutableMapOf<String, Int>()
    val partitions = Partitions(transaction { query().count() }, 0.7, 0.15)
    partitions.print()
    partitions.print2()

    transaction {
        val info = DatasetInfo.fromPartitions(partitions, where().toString())
        query()
            .orderBy(DbSatds.id)
            .forEachIndexed { idx, it ->
                val folder = partitions.sequence[idx]
                val index = typeIndexes.getOrDefault(folder, 0) + 2
                typeIndexes[folder] = index
                val satdId = it[DbSatds.id].value
                info.addSatdId(folder, satdId)
                writeSource(satdId, folder, "satd", it[DbSatds.old_clean], index - 1)
                writeSource(satdId, folder, "fixed", it[DbSatds.new_clean], index)
            }
        info.saveTo(infoFile)
    }

}

internal class Partitions(val count: Int, val trainPerc: Double, val testPerc: Double) {

    companion object {
        const val training = "training"
        const val validation = "validation"
        const val test = "test"
        val all = listOf(training, validation, test)
    }

    init {
        assert2(trainPerc + testPerc <= 1.0)
    }

    val trainCount = (count * trainPerc).toInt()
    val testCount = (count * testPerc).toInt()
    val validationCount = (count - (trainCount + testCount))


    val sequence = (repeatString(training, trainCount)
            + repeatString(validation, validationCount)
            + repeatString(test, testCount))
        .shuffled()

    private fun repeatString(str: String, count: Int) = List(count) { str }

    fun print() {
        println("Dataset $validationCount + $testCount + $trainCount  = $count (validation + test + train = total)")
    }

    fun print2() {
        println("Files ${validationCount * 2} + ${testCount * 2} + ${trainCount * 2} = ${count * 2} (validation + test + train = total)")
    }
}


package satd.step2

import com.github.javaparser.JavaParser
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import core.Shutdown
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.config
import satd.utils.logln
import satd.utils.loglnStart
import java.io.BufferedWriter
import java.io.File
import kotlin.system.exitProcess


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
//                and new_clean_len.less(10)
//                and old_clean_len.less(10)
                and old_clean_token_count.less(70)
                and new_clean_token_count.less(70)
                //and clean_diff_ratio.less(0.25)
                and valid.eq(1)
                and accept.eq(1)
                //and url.inList(urlsIssuesGreaterThan100())
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

object MainGenDatasetArgs {
    class Args(parser: ArgParser) {
        val clean_token_count_limit by parser.storing(
            "--clean_token_count_limit",
            help = "select only token_count_limit less than"
        ) { toIntOrNull() }.default { null }
        val exit_status by parser.storing(
            "--exit_status",
            help = "just exit immediatly with the specified exit status; for testing purposes"
        ) { toIntOrNull() }.default { null }

    }

    @JvmStatic
    fun main(args: Array<String>) = mainBody {


        val sw = ArgParser(args).parseInto(::Args)
        sw.exit_status?.also {
            println("Exiting with status $it")
            exitProcess(it)
        }

        val limit = sw.clean_token_count_limit ?: throw SystemExitException("Invalid arguments. Try option -h", 1)
        println("Dataset generation start")
        val where by lazy {
            DbSatds.run {
                (parent_count.eq(1)
                        and old_clean_token_count.less(limit)
                        and new_clean_token_count.less(limit)
                        //and clean_diff_ratio.less(0.25)
                        and valid.eq(1)
                        and accept.eq(1)
                        )
            }
        }
        Generate(breakMode = false, limit = false) { where }.filesWithJavaFeatures()
        Shutdown.addApplicationShutdownHook { println("Generation done") }
    }
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

fun generate(breakMode: Boolean = false, limit: Boolean = false, where: () -> Op<Boolean>) =
    Generate(breakMode, limit, where).filesForJavaExtractor()

class Generate(val breakMode: Boolean = false, val limit: Boolean = false, val where: () -> Op<Boolean>) {

    val mainImportPredictions = MainImportPredictions()
    val workFolder = mainImportPredictions.folder
    val infoFile = mainImportPredictions.infoFile
    val javaFeatureFolder = File(config.code2vec_path)


    private fun writeSource(
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

            val breakString = if (breakMode && index % 2 == 0) " break! " else ""
            folder.resolve(filename).writeText(wrapMethod(method.toString() + breakString))
        } catch (ex: Exception) {
            println("$filename\n$content")
            throw ex
        }
    }

    fun filesWithJavaFeatures() {

        loglnStart("GenDataset-filesForJavaExtractor")
        logln("Using workFolder: $workFolder")
        workFolder.deleteRecursively()
        assert2(!workFolder.exists())

        workFolder.mkdirs()
        persistence.setupDatabase()

        fun query() = DbSatds.run {
            slice(id, old_clean, new_clean, old_clean_features, new_clean_features).select { where() }
                .orderBy(DbSatds.id).let { if (limit) it.take(100) else it }
        }


        val typeIndexes = mutableMapOf<String, Int>()
        val testFeat = File(javaFeatureFolder, "java-small.test.raw.txt")
        val valiFeat = File(javaFeatureFolder, "java-small.val.raw.txt")
        val traiFeat = File(javaFeatureFolder, "java-small.train.raw.txt")
        listOf(testFeat, valiFeat, traiFeat)
            .map { it.absoluteFile.normalize() }
            .apply {
                forEach { delete(it) }
                map { File(it.absolutePath + ".full") }.forEach { delete(it) }

            }
//        println("ok cancellati")
//        readLine()
        val traiApp = traiFeat.bufferedWriter()
        val valiApp = valiFeat.bufferedWriter()
        val testApp = testFeat.bufferedWriter()

        transaction {
            val recordCount = query().count()
            val partitions = Partitions(recordCount, 0.7, 0.15)
            partitions.print()
            partitions.print2()

            val info = DatasetInfo.fromPartitions(partitions, where().toString())
            query()
                .forEachIndexed { idx, it ->
                    val folder = partitions.sequence[idx]
                    val index = typeIndexes.getOrDefault(folder, 0) + 2
                    typeIndexes[folder] = index
                    val satdId = it[DbSatds.id].value
                    info.addSatdId(folder, satdId)
                    if (folder == Partitions.test) {
                        writeSource(satdId, folder, "satd", it[DbSatds.old_clean], index - 1)
                        writeSource(satdId, folder, "fixed", it[DbSatds.new_clean], index)
                    }

                    val app = when (folder) {
                        Partitions.validation -> valiApp
                        Partitions.training -> traiApp
                        Partitions.test -> testApp
                        else -> error("unrecognized folder $folder")
                    }
                    append(app, "satd", it[DbSatds.old_clean_features])
                    append(app, "fixed", it[DbSatds.new_clean_features])


                }
            valiApp.close()
            traiApp.close()
            testApp.close()
            info.saveTo(infoFile)
        }
    }

    private fun append(buf: BufferedWriter, type: String, features: String) {
        val f = features.split("\t", limit = 2)[1]
        val f2 = type + " " + f.substringAfter(" ")
//        println(features)
//        println("  \t$f2")
        buf.appendln(f2)

    }

    private fun delete(it: File) {
        println("delete $it")
        if (it.exists()) assert2(it.delete())
    }

    fun filesForJavaExtractor() {

        loglnStart("GenDataset-filesWithJavaFeatures")
        logln("Using workFolder: $workFolder")
        workFolder.deleteRecursively()
        assert2(!workFolder.exists())

        workFolder.mkdirs()
        persistence.setupDatabase()

        fun query() = DbSatds.select { where() }.orderBy(DbSatds.id).let { if (limit) it.take(100) else it }

        val typeIndexes = mutableMapOf<String, Int>()
        val partitions = Partitions(transaction { query().count() }, 0.7, 0.15)
        partitions.print()
        partitions.print2()

        transaction {
            val info = DatasetInfo.fromPartitions(partitions, where().toString())
            query()
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


package satd.step2

import core.Shutdown
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import satd.utils.config
import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit


object MainGenAndEval {

    fun whereToken(token_count: Int): Op<Boolean> =
        DbSatds.run {
            (parent_count.eq(1)
                    and old_clean_token_count.less(token_count)
                    and new_clean_token_count.less(token_count)
                    and valid.eq(1)
                    and accept.eq(1)
                    )
        }

    @JvmStatic
    fun main(args: Array<String>) {
        Shutdown.hook()
        persistence.setupDatabase()
        execute(400)
//        (11..20).forEach {
//            val token_count = it * 10
//            execute(token_count)
//        }

    }

    private fun execute(token_count: Int) {
        val workingDir = File(config.code2vec_path).absoluteFile.normalize()
        Generate(breakMode = false, limit = false) { whereToken(token_count) }.filesWithJavaFeatures()

        val output = workingDir.run {
            val conda = "bash"
            arrayOf(
                runCommand("$conda ./preprocess-only-histograms.sh"),
                runCommand("$conda ./train.sh"),
                runCommand("$conda ./evaluate_trained_model.sh")
            ).joinToString("\n\n" + "-".repeat(100) + "\n\n")
        }
        val imp = MainImportPredictions()
        imp.outputFile.writeText(output)
        imp.evaluationCopyInDb()
    }

}

object TestOutput {

    @JvmStatic
    fun main(args: Array<String>) {
        Shutdown.hook()
        val workingDir = File(config.code2vec_path).absoluteFile.normalize()
        workingDir.run {
            val conda = "bash"
            runCommand("$conda -c ./test_output.py")
        }
    }

}

private fun File.runCommand(command: String): String {
    println("Going to run [$command] in work folder [$this]")
    val pb = ProcessBuilder(*command.split(" ").toTypedArray())
        .directory(this)

    pb.redirectErrorStream(true)
    val process = pb.start()

    val inputStream = process.inputStream
    val accumulator = StringBuilder()
    val th = Thread {
        try {
            val pr = inputStream.bufferedReader().use { lines ->
                lines.lines().forEach {
                    println(it)
                    accumulator.appendln(it)
                }
            }
        } catch (ex: Exception) {
            println("INPUT STREAM EXCEPTION $ex")
        }
    }.apply {
        isDaemon = true
        start()
    }
    (1..(3600 * 3)).forEach {
        val end = process.waitFor(1, TimeUnit.SECONDS)
        if (Shutdown.isShuttingDown)
            process.destroyForcibly()
        if (end) {
            val exitValue = process.exitValue()
            println("process ended. exitValue=$exitValue")
            while (th.isAlive)
                Thread.sleep(100)
            assert2(exitValue == 0)

            return accumulator.toString()
        }

    }
    return "TIMEOUT\n\n$accumulator\n\nTIMEOUT"
}

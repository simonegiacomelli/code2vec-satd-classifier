package satd.step2

import core.Shutdown
import satd.utils.config
import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit


object MainGenAndEval {

    @JvmStatic
    fun main(args: Array<String>) {
        Shutdown.hook()
        persistence.setupDatabase()
        val workingDir = File(config.code2vec_path).absoluteFile.normalize()
        Generate(breakMode = false, limit = false) { where1 }.filesWithJavaFeatures()
        val output = workingDir.run {
            val conda = "bash"
            arrayOf(
                runCommand("$conda ./preprocess-only-histograms.sh")
                , runCommand("$conda ./train.sh")
                , runCommand("$conda ./evaluate_trained_model.sh")
            ).joinToString("\n\n" + "-".repeat(100) + "\n\n")
        }
        MainImportPredictions().evaluationCopyInDb(output)
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

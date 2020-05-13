package satd.step2

import core.Shutdown
import satd.utils.config
import java.io.File
import java.util.concurrent.TimeUnit


object MainGenAndEval {
    @JvmStatic
    fun main(args: Array<String>) {
        Shutdown.hook()
        val workingDir = File(config.code2vec_path)
        generate(breakMode = true) { where4 }
        workingDir.run {
//        val conda = "conda run -n code2vec"
            val conda = "bash"
            runCommand("$conda ./preprocess.sh")
            runCommand("$conda ./train.sh")
            runCommand("$conda ./evaluate_trained_model.sh")
        }
        persistence.setupDatabase()
        MainImportPredictions().evaluationCopyInDb()
    }
}

private fun File.runCommand(command: String) {
    println("Going to run [$command]")
    val process = ProcessBuilder(*command.split(" ").toTypedArray())
        .directory(this)
        .inheritIO()
        .start()
    (1..(3600 * 3)).forEach {
        val end = process.waitFor(1, TimeUnit.SECONDS)
        if (Shutdown.isShuttingDown)
            process.destroyForcibly()
        if (end) {
            assert2(process.exitValue() == 0)
            return
        }

    }
}

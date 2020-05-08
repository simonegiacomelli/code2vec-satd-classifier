package satd.step2.perf

import satd.step2.*
import satd.utils.config
import java.io.File
import java.util.concurrent.TimeUnit

fun main() {
    val workingDir = File(config.code2vec_path)
    generate { where3 }
    workingDir.run {
        val conda = "conda run -n code2vec"
        runCommand("$conda ./preprocess.sh.exec.sh")
        runCommand("$conda ./train.sh.exec.sh")
        runCommand("$conda ./evaluate_trained_model.sh")
    }
    persistence.setupDatabase()
    MainImportPredictions().evaluationCopyInDb()
}


private fun File.runCommand(command: String) {
    println("Going to run [$command]")
    val process = ProcessBuilder(*command.split(" ").toTypedArray())
        .directory(this)
        .inheritIO()
        .start()
    val end = process.waitFor(3, TimeUnit.HOURS)
    assert2(end)
    assert2(process.exitValue() == 0)

}

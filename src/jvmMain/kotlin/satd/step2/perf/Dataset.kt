package satd.step2.perf

import satd.utils.Folders
import satd.utils.config
import java.io.File

class Dataset {
    val folder: File = File(config.dataset_export_path ?: Folders.dataset.resolve("java-small").toString())
    val evaluated: File = File("$folder-evaluated")

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

        }
    }

}
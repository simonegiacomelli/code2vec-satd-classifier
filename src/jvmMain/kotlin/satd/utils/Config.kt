package satd.utils

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

val config = Config()

class Config(private val workingDirectory: String = ".") {

    val repos_path by this
    val thread_count by this
    val batch_size by this
    val code2vec_path by notNull("please specify code2vec path")
    val dataset_export_path get()= "$code2vec_path/build-dataset/java-small"


    val prop by lazy {
        Properties().also { p ->
            loadConfFile(p, "config.properties")
            loadConfFile(p, "config_$hostname.properties")
            p.forEach {
                satd.utils.logln("configuration properties: ${it.key}=${it.value}")
            }
        }
    }

    private fun loadConfFile(prop: Properties, filename: String) {
        File(workingDirectory, filename).canonicalFile.apply {
            if (exists())
                inputStream()
                    .use {
                        logln("Loading: $absolutePath")
                        prop.load(it)
                    }
            else
                logln("Config file does not exists: $absolutePath")
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? = prop.getProperty(property.name)

    inner class notNull(val msg: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
            prop.getProperty(property.name) ?: error(msg)
    }

}

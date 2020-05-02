package satd.utils

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

class Config(val workingDirectory: String = ".") {
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
        File(workingDirectory, filename).apply {
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

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        return prop.getProperty(property.name)
    }

    val repos_path by this
    val thread_count by this
    val batch_size by this
    val dataset_export_path by this
}

val config = Config()
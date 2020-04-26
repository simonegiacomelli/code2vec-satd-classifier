package satd.utils

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

class Config {
    val prop = Properties()
    private val initialized = lazy { true }
    fun load() {
        loadConfFile("config.properties")
        loadConfFile("config_$hostname.properties")
        prop.forEach {
            logln("configuration properties: ${it.key}=${it.value}")
        }
        initialized.value
    }

    private fun loadConfFile(filename: String) {
        File(filename).apply {
            if (exists())
                inputStream()
                    .use {
                        logln("Loading: $absolutePath")
                        prop.load(it)
                    }
            else
                logln("Config file does not exists: $absolutePath does")
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        if (!initialized.isInitialized())
            throw UninitializedPropertyAccessException("Config is not loaded.")
        return prop.getProperty(property.name)
    }

    val repos_path by this
    val thread_count by this
    val batch_size by this
    val dataset_export_path by this
}

val config = Config()
package satd.utils

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

class Config {
    private val prop = Properties()
    private val initialized = lazy { true }
    fun loadArgs(args: Array<String>) {
        load("config.properties")
        load("config_$hostname.properties")
        if (args.isNotEmpty()) load(args[0])
        prop.forEach {
            logln("configuration properties: ${it.key}=${it.value}")
        }
        initialized.value
    }

    private fun load(filename: String) {
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

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        if (!initialized.isInitialized())
            throw UninitializedPropertyAccessException("Config is not loaded.")
        return prop.getProperty(property.name).orEmpty()
    }

    val repos_path by this
    val thread_count by this
    val batch_size by this
    val if_repo_exists_check_integrity by this
}

val config = Config()
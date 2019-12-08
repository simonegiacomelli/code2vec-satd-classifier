package satd.utils

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

class Config {
    private val prop = Properties()
    private val initialized = lazy { true }
    fun loadArgs(args: Array<String>) {
        File(if (args.isEmpty()) "config.properties" else args[0])
            .apply {
                if (exists())
                    inputStream()
                        .use {
                            logln("Loading: $absolutePath")
                            prop.load(it)
                        }
                else
                    logln("Config file does not exists: $absolutePath does")
            }
        initialized.value
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        if (!initialized.isInitialized())
            throw UninitializedPropertyAccessException("Config is not loaded.")
        return prop.getProperty(property.name).orEmpty()
    }

    val repos_path by this
    val thread_count by this
    val batch_size by this
}

val config = Config()
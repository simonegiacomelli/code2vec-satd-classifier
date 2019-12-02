package satd.utils

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

class Config {
    private val prop = Properties()


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
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return prop.getProperty(property.name).orEmpty()
    }

    val repos_path by this
}

val config = Config()
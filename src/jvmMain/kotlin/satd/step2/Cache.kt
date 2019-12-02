package satd.step2

import satd.utils.Folders
import satd.utils.AntiSpin
import java.util.*

open class Cache(type: String, context: String) {

    val folder = Folders.cache.resolve(type)
    val filename = folder.resolve("$context.txt").toFile()

    val map = mutableMapOf<String, String>()


    operator fun get(name: String): String? = map[name]

    operator fun set(name: String, value: String) {
        map[name] = value
    }

    fun store() {
        folder.toFile().mkdirs()
        filename.bufferedWriter().use {
            //write header
            it.appendln("size=${map.size} //${Date()}")
            it.appendln()
            for (e in map)
                it.appendln("${e.key}\t${e.value}")
        }
    }

    fun load() {
        map.clear()
        if (filename.exists()) {
            filename
                .bufferedReader()
                .useLines {
                    val seq = it.dropWhile { line -> line != "" }.drop(1)

                    seq.forEach {
                        val kv = it.split("\t")
                        map[kv[0]] = kv[1]
                    }
                }
        }
    }


}

class CacheSpin(type: String, context: String) : Cache(type, context) {
    private val antiSpin = AntiSpin(windowMillis = 10000) { store() }
    fun storeSpin() = antiSpin.spin()
}
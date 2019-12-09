package satd.step2

import satd.utils.AntiSpin
import java.io.File
import java.util.*

open class Cache(val file: File) {

    val folder = file.parentFile
    val map = mutableMapOf<String, String>()


    operator fun get(name: String): String? = map[name]

    operator fun set(name: String, value: String) {
        map[name] = value
    }

    fun store() {
        folder.mkdirs()
        file.bufferedWriter().use {
            //write header
            it.appendln("size=${map.size} //${Date()}")
            it.appendln()
            for (e in map)
                it.appendln("${e.key}\t${e.value}")
        }
    }

    fun load() {
        map.clear()
        if (exists()) {
            file
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

    fun exists() = file.exists()


}

class CacheSpin(file: File) : Cache(file) {
    private val antiSpin = AntiSpin(windowMillis = 10000) { store() }
    fun storeSpin() = antiSpin.spin()
}
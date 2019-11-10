package satd.step2

import satd.step1.Folders

class Cache(val type: String, val context: String) {

    val folder = Folders.cache.resolve(type)
    val filename = folder.resolve(context).toFile()

    val map = mutableMapOf<String, String>()


    operator fun get(name: String): String? = map[name]

    operator fun set(name: String, value: String) {
        map[name] = value
    }

    fun store() {
        folder.toFile().mkdirs()
        filename.bufferedWriter().use {
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
                    it.forEach {
                        val kv = it.split("\t")
                        map[kv[0]] = kv[1]
                    }
                }
        }
    }
}
package satd.utils

import kotlin.math.ln
import kotlin.math.pow


fun main() {
    HeapDumper.enable()

    val rt = Runtime.getRuntime()
    val container = ArrayContainer()
    while (true) {
        println("""Free Mem: ${rt.freeMemory().hr} used: ${rt.usedMemory.hr}""")
        container.list.add(ArrayWrapper(IntArray(1024 * 1024 * 10)))
    }
}

class ArrayContainer {
    val list = mutableListOf<ArrayWrapper>()
}

data class ArrayWrapper(val a: IntArray)

private val Runtime.usedMemory: Long get() = totalMemory() - freeMemory()

val Int.hr: String get() = this.toLong().hr
val Long.hr: String
    get() {
        val si = false
        val bytes = this
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }
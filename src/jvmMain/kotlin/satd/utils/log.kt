package satd.utils

fun logln(line: String) {
    val name = Thread.currentThread().name
    val message = "${name.padEnd(50)} $line"
    println(message)
}
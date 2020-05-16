package sample

fun main() {
    val l = "a = 1\na= 2=pippo\nb=2"
        .split("\n")
        .map { line ->
            line.split("=", limit = 2).let { it[0].trim() to it[1] }
        }.toMap()

    println(l)
}
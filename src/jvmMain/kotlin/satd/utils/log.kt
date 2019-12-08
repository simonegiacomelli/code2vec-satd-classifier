package satd.utils

private val logfile by lazy {
    val f = Folders.log.toFile().resolve("run")
    f.mkdirs()
    f.resolve("${dateTimeToStr()}-p$pid.txt").printWriter()
}

@Synchronized
fun logln(line: String) {
    val name = Thread.currentThread().name
    val message = "${name.padEnd(4)} $line"
    println(message)
    logfile.println(message)
    logfile.flush()
}
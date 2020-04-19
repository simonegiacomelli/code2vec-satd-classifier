package satd.utils

private var appname = "default-log"
fun loglnStart(name: String) {
    appname = name
    logln("Starting $name")
    logln("pid: $pid")
}

private val logfile by lazy {
    val f = Folders.log.toFile().resolve("run")
    f.mkdirs()
    f.resolve("$appname${dateTimeToStr()}-p$pid.txt").printWriter()
}

@Synchronized
fun logln(line: String) {
    val name = Thread.currentThread().name
    val message = "${name.padEnd(4)} $line"
    println(message)
    logfile.println(message)
    logfile.flush()
}
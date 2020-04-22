package core

/* Simone 25/08/13 8.25 */
class Daemon {
    fun start(name: String?, runnable: Runnable?) {
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread.name = name
        thread.start()
    }
}
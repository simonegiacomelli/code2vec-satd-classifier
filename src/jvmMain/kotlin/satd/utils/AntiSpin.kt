package satd.utils

class AntiSpin(val windowMillis: Int = 1000, val callback: () -> Unit) {
    val time: () -> Long = System::currentTimeMillis

    private fun currentSecond(): Int {
        val current = time()
        val currentWindow = (current / windowMillis).toInt()
        return currentWindow
    }

    var lastSecond = -1
    fun spin() {
        val currentSecond = currentSecond()
        if (lastSecond != currentSecond) {
            lastSecond = currentSecond
            callback()
        }
    }
}
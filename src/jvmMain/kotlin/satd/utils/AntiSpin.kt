package satd.utils

class AntiSpin(val callback: () -> Unit) {
    val time: () -> Long = System::currentTimeMillis

    private fun currentSecond(): Int {
        val current = time()
        val currentSecond = (current / 1000).toInt()
        return currentSecond
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
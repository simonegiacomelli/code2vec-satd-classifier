package satd.utils

import java.util.*

/**
 * Keep track of spin per second in a time window
 */
class Rate(val windowSecs: Int, val time: () -> Long = System::currentTimeMillis) {
    val counter = LinkedList<Int>()
    var spinCount = 0
    var trackedSecond: Int = -1
    var secs = 0

    init {
        reset()
    }

    fun reset(): Rate {
        counter.clear()
        counter.push(0)
        trackedSecond = currentSecond()
        return this
    }

    fun spin() {
        spinCount++
        update()
        counter[0]++
    }

    private fun update() {
        val currentSecond = currentSecond()

        secs = currentSecond - trackedSecond

        repeat(secs) { counter.addFirst(0) }

        val exceedSize = counter.size - windowSecs
        repeat(exceedSize) { counter.removeLast() }
        trackedSecond = currentSecond
    }

    private fun currentSecond(): Int {
        val current = time()
        val currentSecond = (current / 1000).toInt()
        return currentSecond
    }

    fun rate(): Double {
        update()
        if (counter.isEmpty()) return .0
        return counter.sum().toDouble() / counter.size
    }

    override fun toString(): String {
        return "%.3f".format(rate())
    }
}
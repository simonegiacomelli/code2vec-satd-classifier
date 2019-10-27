package satd.step2

import java.util.*

/**
 * Keep track of spin per second in a time window
 */
class Speed(val window: Int, val time: () -> Long = System::currentTimeMillis) {
    val counter = LinkedList<Int>()

    var trackedSecond: Int = -1
    var secs = 0

    init {
        reset()
    }

    fun reset(): Speed {
        counter.clear()
        counter.push(0)
        trackedSecond = currentSecond()
        return this
    }

    fun spin() {
        val currentSecond = currentSecond()

        secs = currentSecond - trackedSecond

        repeat(secs) { counter.addFirst(0) }

        val exceedSize = counter.size - window
        repeat(exceedSize) { counter.removeLast() }

        counter[0]++

        trackedSecond = currentSecond
    }

    private fun currentSecond(): Int {
        val current = time()
        val currentSecond = (current / 1000).toInt()
        return currentSecond
    }

    fun rate(): Double {
        if (counter.isEmpty()) return .0
        return counter.sum().toDouble() / counter.size
    }


}
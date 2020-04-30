package satd.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class RateTest {

    @Test
    fun lowRate() {
        val (clock, target) = newTarget()

        target.spin()
        target.spin()
        assertEquals(2.0, target.rate())
    }

    @Test
    fun twoSlots() {
        val (clock, target) = newTarget()


        target.spin()
        target.spin()
        target.spin()
        clock.time += 1001
        target.spin()
        target.spin()
        target.spin()

        assertEquals(3.0, target.rate())
    }

    @Test
    fun testCircularBuffer() {
        val (clock, target) = newTarget()


        (1..40).forEach {
            clock.time += 1001
            target.spin()
            target.spin()
            target.spin()
        }
        assertEquals(3.0, target.rate())
    }

    @Test
    fun noSpin() {
        val (clock, target) = newTarget()

        target.spin()
        clock.time += 1001


        assertEquals(0.0, target.rate())
    }

    private fun newTarget(): Pair<FakeClock, Rate> {
        val clock = FakeClock()
        val target = Rate(1, time = clock::get)
        return Pair(clock, target)
    }

    @Test
    fun `reset should set spinCount to 0`(){
        val clock = FakeClock()
        val target = Rate(1, time = clock::get)
        target.spin()
        target.reset()

        assertEquals(0,target.spinCount)
    }

    class FakeClock {
        var time = 0L
        fun get() = time
    }
}
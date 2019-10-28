package satd.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class RateTest {

    @Test
    fun lowRate() {
        val clock = FakeClock()
        val target = Rate(10, clock::get)

        target.spin()
        target.spin()
        assertEquals(2.0, target.rate())
    }

    @Test
    fun twoSlots() {
        val clock = FakeClock()
        val target = Rate(10, clock::get)


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
        val clock = FakeClock()
        val target = Rate(10, clock::get)


        (1..40).forEach {
            clock.time += 1001
            target.spin()
            target.spin()
            target.spin()
        }
        assertEquals(3.0, target.rate())
    }

    class FakeClock {
        var time = 0L
        fun get() = time
    }
}
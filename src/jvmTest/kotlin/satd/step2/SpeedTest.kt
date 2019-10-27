package satd.step2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SpeedTest {

    @Test
    fun lowRate() {
        val clock = FakeClock()
        val target = Speed(10, clock::get)

        target.spin()
        target.spin()
        assertEquals(2.0, target.rate())
    }

    @Test
    fun b() {
        val clock = FakeClock()
        val target = Speed(10, clock::get)


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
        val target = Speed(10, clock::get)


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
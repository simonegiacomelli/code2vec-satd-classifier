package satd.github


import org.joda.time.DateTime
import kotlin.test.*

class DateRangeTest {

    @Test
    fun testDays() {
        val dtStart = DateTime.parse("2020-01-01")
        val dtEnd = DateTime.parse("2020-01-06")

        val target = DateRange(dtStart, dtEnd)
        assertEquals(6, target.days)
    }

    @Test
    fun testExternalBorderDatesAreCorrect() {
        val dtStart = DateTime.parse("2020-01-01")
        val dtEnd = DateTime.parse("2020-01-06")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals(dtStart, left.dtStart)
        assertEquals(dtEnd, right.dtEnd)
    }

    @Test
    fun testInternalBordersAreCorrect() {
        val dtStart = DateTime.parse("2020-01-01")
        val dtEnd = DateTime.parse("2020-01-06")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals(DateTime.parse("2020-01-03"), left.dtEnd)
        assertEquals(DateTime.parse("2020-01-04"), right.dtStart)
    }

    @Test
    fun testSameDay_ExternalBorderDatesAreCorrect() {
        val dtStart = DateTime.parse("2020-01-01")
        val dtEnd = DateTime.parse("2020-01-01")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals("2020-01-01T00:00:00Z", left.start)
        assertEquals("2020-01-01T23:59:59Z", right.end)
    }

    @Test
    fun testSameDay_InternalBorderDatesAreCorrect() {
        val dtStart = DateTime.parse("2020-01-01")
        val dtEnd = DateTime.parse("2020-01-01")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals("2020-01-01T11:59:59Z", left.end)
        assertEquals("2020-01-01T12:00:00Z", right.start)
    }

    @Test
    fun testSameDay_withTime_ExternalBorderDatesAreCorrect() {
        val dtStart = DateTime.parse("2020-01-01T01:02:03")
        val dtEnd = DateTime.parse("2020-01-01T12:13:14")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals("2020-01-01T01:02:03Z", left.start)
        assertEquals("2020-01-01T12:13:14Z", right.end)
    }

    @Test
    fun testSameDay_withTime_InternalBorderDatesAreCorrect() {
        val dtStart = DateTime.parse("2020-01-01T00:00:03")
        val dtEnd = DateTime.parse("2020-01-01T00:00:08")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals("2020-01-01T00:00:05Z", left.end)
        assertEquals("2020-01-01T00:00:06Z", right.start)
    }

    @Test
    fun testSameDay_withTime_TotalIsCorrect() {
        val dtStart = DateTime.parse("2020-01-01T00:00:03")
        val dtEnd = DateTime.parse("2020-01-01T00:00:04")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals(target.seconds, left.seconds + right.seconds)
    }

    @Test
    fun testSameDay_withTime_TotalIsCorrect_odd() {
        val dtStart = DateTime.parse("2020-01-01T00:00:03")
        val dtEnd = DateTime.parse("2020-01-01T00:00:05")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals(target.seconds, left.seconds + right.seconds)
    }

    @Test
    fun testTotalIsCorrect() {
        val dtStart = DateTime.parse("2000-01-01")
        val dtEnd = DateTime.parse("2021-12-31")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals(target.days, left.days + right.days)
    }

    @Test
    fun testDameDay() {
        val dtStart = DateTime.parse("2000-01-01")
        val dtEnd = DateTime.parse("2000-01-01")

        val target = DateRange(dtStart, dtEnd)
        assertTrue(target.sameDay())
    }

    @Test
    fun testDameDayWithTime() {
        val dtStart = DateTime.parse("2000-01-01T01:01:01")
        val dtEnd = DateTime.parse("2000-01-01")

        val target = DateRange(dtStart, dtEnd)
        assertTrue(target.sameDay())
    }

}
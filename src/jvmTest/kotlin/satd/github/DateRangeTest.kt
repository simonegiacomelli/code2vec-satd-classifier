package satd.github


import org.joda.time.DateTime
import kotlin.test.*

class DateRangeTest {

    @Test
    fun testDays(){
        val dtStart = DateTime.parse("2020-01-01")
        val dtEnd = DateTime.parse("2020-01-06")

        val target = DateRange(dtStart, dtEnd)
        assertEquals(6,target.days)
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
    fun testTotalIsCorrect() {
        val dtStart = DateTime.parse("2000-01-01")
        val dtEnd = DateTime.parse("2021-12-31")

        val target = DateRange(dtStart, dtEnd)
        val (left, right) = target.split()
        assertEquals(target.days,left.days+right.days)
    }

    @Test
    fun testDameDay() {
        val dtStart = DateTime.parse("2000-01-01")
        val dtEnd = DateTime.parse("2000-01-01")

        val target = DateRange(dtStart, dtEnd)
        assertTrue(target.sameDay())
    }

}
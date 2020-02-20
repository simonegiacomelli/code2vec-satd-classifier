package satd.github


import org.joda.time.DateTime
import kotlin.test.*

class DateRangeTest {

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

}
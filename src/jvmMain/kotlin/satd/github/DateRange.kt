package satd.github

import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalTime
import kotlin.math.max

class DateRange(val dtStart: DateTime, val dtEnd: DateTime) {
    fun split(): Pair<DateRange, DateRange> {
        if (sameDay()) return splitSameDay()

        val days1 = days / 2
        val days2 = days - days1
        val days = max(days1, days2) - 1

        val left = DateRange(dtStart, dtStart.plusDays(days))
        val right = DateRange(dtStart.plusDays(days + 1), dtEnd)
        return Pair(left, right)
    }

    private fun splitSameDay(): Pair<DateRange, DateRange> {
        val secs1 = seconds / 2
        val secs2 = seconds - secs1
        val secs = max(secs1, secs2) - 1

        val left = DateRange(dtStart, dtStart.plusSeconds(secs))
        val right = DateRange(dtStart.plusSeconds(secs + 1), dtEndAdjusted)
        return Pair(left, right)
    }

    val days: Int = Interval(dtStart, dtEnd.plusDays(1)).toDuration().toStandardDays().days

    private fun DateTime.yyyyMMdd() = toString("yyyy-MM-dd").orEmpty()

    private fun DateTime.format() = if (!sameDay()) yyyyMMdd() else
        toString("yyyy-MM-dd'T'HH:mm:ss'Z'").orEmpty()


    val start = dtStart.format()
    val end = dtEnd.format()
    val qry = "$start..$end"
    val fs = "$start--$end"

    private val dtEndAdjusted: DateTime = if (dtEnd.toLocalTime() == LocalTime.MIDNIGHT)
        dtEnd.plusDays(1).minusSeconds(1) else dtEnd
    val seconds: Int = Interval(dtStart, dtEndAdjusted.plusSeconds(1)).toDuration().toStandardSeconds().seconds

    override fun toString() = qry
    fun sameDay() = dtStart.yyyyMMdd() == dtEnd.yyyyMMdd()
}
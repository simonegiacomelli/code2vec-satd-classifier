package satd.github

import org.joda.time.DateTime
import org.joda.time.Interval
import kotlin.math.max

class DateRange(val dtStart: DateTime, val dtEnd: DateTime) {
    fun split(): Pair<DateRange, DateRange> {
        if (dtStart == dtEnd) throw Exception("Cannot split one day :(")

        val days1 = days / 2
        val days2 = days - days1
        val days = max(days1, days2) - 1

        val left = DateRange(dtStart, dtStart.plusDays(days))
        val right = DateRange(dtStart.plusDays(days + 1), dtEnd)
        return Pair(left, right)
    }

    val days: Int = Interval(dtStart, dtEnd.plusDays(1)).toDuration().toStandardDays().days

    private fun DateTime.yyyyMMdd() = toString("yyyy-MM-dd").orEmpty()

    val start = dtStart.yyyyMMdd()
    val end = dtEnd.yyyyMMdd()
    val qry = "$start..$end"
    val fs = "$start--$end"

    override fun toString() = qry
}
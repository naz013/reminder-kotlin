package com.github.naz013.icalendar.parser.common

import com.github.naz013.icalendar.ByDayRecurParam
import com.github.naz013.icalendar.ByHourRecurParam
import com.github.naz013.icalendar.ByMinuteRecurParam
import com.github.naz013.icalendar.ByMonthDayRecurParam
import com.github.naz013.icalendar.ByMonthRecurParam
import com.github.naz013.icalendar.BySetPosRecurParam
import com.github.naz013.icalendar.ByWeekNumberRecurParam
import com.github.naz013.icalendar.ByYearDayRecurParam
import com.github.naz013.icalendar.CountRecurParam
import com.github.naz013.icalendar.Day
import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqRecurParam
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.IntervalRecurParam
import com.github.naz013.icalendar.RecurParam
import com.github.naz013.icalendar.RecurParamType
import com.github.naz013.icalendar.UntilRecurParam
import com.github.naz013.icalendar.WeekStartRecurParam

internal class RecurParamParser {

  private val utcDateTimeParser = UtcDateTimeParser()

  private val intTransform: (String) -> Int = { it.toInt() }

  fun parse(input: String): List<RecurParam> {
    return input.split(";").mapNotNull { parsePart(it) }
  }

  private fun parsePart(input: String): RecurParam? {
    if (!input.contains("=")) return null
    val parts = input.split("=")
    val type = runCatching { parseType(parts[0]) }.getOrNull() ?: return null
    return runCatching { parseValue(parts[1], type) }.getOrNull()
  }

  private fun parseType(input: String): RecurParamType {
    return RecurParamType.entries.first { it.value == input }
  }

  private fun parseValue(input: String, type: RecurParamType): RecurParam? {
    return when (type) {
      RecurParamType.COUNT -> CountRecurParam(input.toInt())
      RecurParamType.INTERVAL -> IntervalRecurParam(input.toInt())
      RecurParamType.FREQ -> FreqRecurParam(
        FreqType.entries.first { it.value == input }
      )
      RecurParamType.UNTIL -> utcDateTimeParser.parse(input)?.let { UntilRecurParam(it) }
      RecurParamType.BYDAY -> ByDayRecurParam(parseArray(input) { DayValue(it) })
      RecurParamType.BYMONTH -> ByMonthRecurParam(parseIntArray(input))
      RecurParamType.BYMONTHDAY -> ByMonthDayRecurParam(parseIntArray(input))
      RecurParamType.BYHOUR -> ByHourRecurParam(parseIntArray(input))
      RecurParamType.BYMINUTE -> ByMinuteRecurParam(parseIntArray(input))
      RecurParamType.BYYEARDAY -> ByYearDayRecurParam(parseIntArray(input))
      RecurParamType.BYWEEKNO -> ByWeekNumberRecurParam(parseIntArray(input))
      RecurParamType.WEEKSTART -> WeekStartRecurParam(
        DayValue(
          Day.entries.first { it.value == input }
        )
      )
      RecurParamType.BYSETPOS -> BySetPosRecurParam(parseIntArray(input))
    }
  }

  private fun parseIntArray(input: String): List<Int> {
    return parseArray(input, intTransform)
  }

  private fun <T> parseArray(input: String, transform: (String) -> T): List<T> {
    return input.split(",").map(transform)
  }
}

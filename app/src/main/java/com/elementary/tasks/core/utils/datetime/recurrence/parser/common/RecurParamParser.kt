package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

import com.elementary.tasks.core.utils.datetime.recurrence.ByDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByHourRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMinuteRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.BySetPosRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByWeekNumberRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByYearDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.CountRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.Day
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.core.utils.datetime.recurrence.FreqRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.core.utils.datetime.recurrence.IntervalRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParamType
import com.elementary.tasks.core.utils.datetime.recurrence.UntilRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.WeekStartRecurParam

class RecurParamParser {

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
    return RecurParamType.values().first { it.value == input }
  }

  private fun parseValue(input: String, type: RecurParamType): RecurParam? {
    return when (type) {
      RecurParamType.COUNT -> CountRecurParam(input.toInt())
      RecurParamType.INTERVAL -> IntervalRecurParam(input.toInt())
      RecurParamType.FREQ -> FreqRecurParam(
        FreqType.values().first { it.value == input }
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
          Day.values().first { it.value == input }
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

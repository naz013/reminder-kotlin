package com.elementary.tasks.core.utils.datetime.recurrence.builder

import com.elementary.tasks.core.utils.datetime.recurrence.ByDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeEndTag
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeStartTag
import com.elementary.tasks.core.utils.datetime.recurrence.Day
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.core.utils.datetime.recurrence.FreqRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.core.utils.datetime.recurrence.IntervalRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceRuleTag
import com.elementary.tasks.core.utils.datetime.recurrence.Tag
import com.elementary.tasks.core.utils.datetime.recurrence.UntilRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import org.junit.Assert.*
import org.junit.Test

class RuleBuilderTest {
  private val ruleBuilder = RuleBuilder()

  @Test
  fun testBuildEmptyList() {
    val result = ruleBuilder.buildString(emptyList())
    assertNull(result)
  }

  @Test
  fun testParseDTStartAndRrule() {
    val tags: List<Tag> = listOf(
      DateTimeStartTag(UtcDateTime("19970714T123000Z")),
      RecurrenceRuleTag(
        listOf(
          FreqRecurParam(FreqType.MONTHLY),
          ByDayRecurParam(listOf(DayValue(Day.SA))),
          ByMonthDayRecurParam(listOf(7, 8, 9, 10, 11, 12, 13))
        )
      )
    )

    val result = ruleBuilder.buildString(tags)

    val expected = "DTSTART:19970714T123000\n" +
      "RRULE:FREQ=MONTHLY;BYDAY=SA;BYMONTHDAY=7,8,9,10,11,12,13"
    assertEquals(expected, result)
  }

  @Test
  fun testParseDTStartAndRruleMultiline() {
    val tags: List<Tag> = listOf(
      DateTimeStartTag(UtcDateTime("19970714T123000Z")),
      RecurrenceRuleTag(
        listOf(
          FreqRecurParam(FreqType.YEARLY),
          IntervalRecurParam(4),
          ByMonthRecurParam(listOf(11)),
          ByDayRecurParam(listOf(DayValue("TU"))),
          ByMonthDayRecurParam(
            listOf(2, 3, 4, 5, 6, 7, 8)
          )
        )
      ),
    )

    val result = ruleBuilder.buildString(tags)

    val expected = "DTSTART:19970714T123000\n" +
      "RRULE:FREQ=YEARLY;INTERVAL=4;BYMONTH=11;BYDAY=TU;BYMONTHDAY=2,3,4,5,6,7,8"
    assertEquals(expected, result)
  }

  @Test
  fun testParseDTStartAndRruleMultilineWithLineSplit() {
    val tags: List<Tag> = listOf(
      DateTimeStartTag(UtcDateTime("19970714T123000Z")),
      RecurrenceRuleTag(
        listOf(
          FreqRecurParam(FreqType.YEARLY),
          IntervalRecurParam(4),
          ByMonthRecurParam(listOf(10, 11, 12)),
          ByDayRecurParam(listOf(DayValue("TU"))),
          ByMonthDayRecurParam(
            listOf(2, 3, 4, 5, 6, 7, 8)
          ),
          UntilRecurParam(UtcDateTime("19970714T123000Z")),
        )
      ),
      DateTimeEndTag(UtcDateTime("19970714T123000Z"))
    )

    val result = ruleBuilder.buildString(tags)

    val expected = "DTSTART:19970714T123000\n" +
      "RRULE:FREQ=YEARLY;INTERVAL=4;BYMONTH=10,11,12;BYDAY=TU;BYMONTHDAY=2,3,4,5,6\n" +
      " ,7,8;UNTIL=19970714T123000\n" +
      "DTEND:19970714T123000"
    assertEquals(expected, result)
  }
}

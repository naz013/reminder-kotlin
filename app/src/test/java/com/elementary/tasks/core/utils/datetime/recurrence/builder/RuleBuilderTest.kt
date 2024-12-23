package com.elementary.tasks.core.utils.datetime.recurrence.builder

import com.github.naz013.icalendar.ByDayRecurParam
import com.github.naz013.icalendar.ByMonthDayRecurParam
import com.github.naz013.icalendar.ByMonthRecurParam
import com.github.naz013.icalendar.DateTimeEndTag
import com.github.naz013.icalendar.DateTimeStartTag
import com.github.naz013.icalendar.Day
import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqRecurParam
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.IntervalRecurParam
import com.github.naz013.icalendar.RecurrenceRuleTag
import com.github.naz013.icalendar.Tag
import com.github.naz013.icalendar.UntilRecurParam
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.icalendar.builder.RuleBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
      )
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
          UntilRecurParam(UtcDateTime("19970714T123000Z"))
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

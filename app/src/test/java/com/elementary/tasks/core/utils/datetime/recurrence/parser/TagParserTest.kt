package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.ByDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeStartTag
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.core.utils.datetime.recurrence.FreqRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.core.utils.datetime.recurrence.IntervalRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceRuleTag
import com.elementary.tasks.core.utils.datetime.recurrence.Tag
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import org.junit.Assert.*
import org.junit.Test

class TagParserTest {

  private val tagParser = TagParser()

  @Test
  fun testParseEmptyTag() {
    val result = tagParser.parse("")
    assertEquals(emptyList<Tag>(), result)
  }

  @Test
  fun testParseDTStartAndRrule() {
    val result = tagParser.parse(
      "DTSTART:19970714T123000Z\n" +
        "RRULE:FREQ=MONTHLY;BYDAY=SA;BYMONTHDAY=7,8,9,10,11,12,13"
    )
    val expected = listOf(
      DateTimeStartTag(UtcDateTime("19970714T123000Z")),
      RecurrenceRuleTag(
        listOf(
          FreqRecurParam(FreqType.MONTHLY),
          ByDayRecurParam(listOf(DayValue("SA"))),
          ByMonthDayRecurParam(
            listOf(7, 8, 9, 10, 11, 12, 13)
          )
        )
      ),
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseDTStartAndRruleMultiline() {
    val result = tagParser.parse(
      "DTSTART:19970714T123000Z\n" +
        "RRULE:FREQ=YEARLY;INTERVAL=4;BYMONTH=11;BYDAY=TU;\n" +
        " BYMONTHDAY=2,3,4,5,6,7,8"
    )
    val expected = listOf(
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
    assertEquals(expected, result)
  }
}

package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.github.naz013.icalendar.ByDayRecurParam
import com.github.naz013.icalendar.ByMonthDayRecurParam
import com.github.naz013.icalendar.ByMonthRecurParam
import com.github.naz013.icalendar.DateTimeStartTag
import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqRecurParam
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.IntervalRecurParam
import com.github.naz013.icalendar.RecurrenceRuleTag
import com.github.naz013.icalendar.Tag
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.icalendar.parser.TagParser
import org.junit.Assert.assertEquals
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
      )
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
      )
    )
    assertEquals(expected, result)
  }
}

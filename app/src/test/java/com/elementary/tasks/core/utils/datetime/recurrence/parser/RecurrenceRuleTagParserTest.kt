package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.github.naz013.icalendar.ByYearDayRecurParam
import com.github.naz013.icalendar.CountRecurParam
import com.github.naz013.icalendar.FreqRecurParam
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.IntervalRecurParam
import com.github.naz013.icalendar.RecurrenceRuleTag
import com.github.naz013.icalendar.parser.RecurrenceRuleTagParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecurrenceRuleTagParserTest {

  private val recurrenceRuleTagParser = RecurrenceRuleTagParser()

  @Test
  fun testParseEmptyTag() {
    val result = recurrenceRuleTagParser.parse("")

    assertNull(result)
  }

  @Test
  fun testParseWrongTag() {
    val result = recurrenceRuleTagParser.parse("DTSTART;TZID=America/New_York:19970913T090000")

    assertNull(result)
  }

  @Test
  fun testParseNormalTag() {
    val result =
      recurrenceRuleTagParser.parse("RRULE:FREQ=YEARLY;INTERVAL=3;COUNT=10;BYYEARDAY=1,100,200")

    val expected = RecurrenceRuleTag(
      params = listOf(
        FreqRecurParam(FreqType.YEARLY),
        IntervalRecurParam(3),
        CountRecurParam(10),
        ByYearDayRecurParam(listOf(1, 100, 200))
      )
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseNormalTag_WrongValue() {
    val result = recurrenceRuleTagParser.parse(
      "RRULE:FREQ=TIMELY;INTERVAL=3;COUNT=10;BYYEARDAY=1,100,200"
    )

    val expected = RecurrenceRuleTag(
      params = listOf(
        IntervalRecurParam(3),
        CountRecurParam(10),
        ByYearDayRecurParam(listOf(1, 100, 200))
      )
    )
    assertEquals(expected, result)
  }
}

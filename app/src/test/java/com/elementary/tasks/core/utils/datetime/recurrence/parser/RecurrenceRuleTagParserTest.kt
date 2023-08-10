package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.ByYearDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.CountRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.FreqRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.core.utils.datetime.recurrence.IntervalRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceRuleTag
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

package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.ParamValueType
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceDateTimeTag
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import com.elementary.tasks.core.utils.datetime.recurrence.ValueParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecurrenceDateTimeTagParserTest {

  private val recurrenceDateTimeTagParser = RecurrenceDateTimeTagParser()

  @Test
  fun testParseEmptyTag() {
    val result = recurrenceDateTimeTagParser.parse("")

    assertNull(result)
  }

  @Test
  fun testParseWrongTag() {
    val result = recurrenceDateTimeTagParser.parse("RRULE:FREQ=DAILY;COUNT=10")

    assertNull(result)
  }

  @Test
  fun testParseNoParamTag() {
    val result = recurrenceDateTimeTagParser.parse("RDATE:19970714T123000Z")

    val expected = RecurrenceDateTimeTag(
      param = null,
      values = listOf(
        UtcDateTime("19970714T123000Z")
      )
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseDateTime() {
    val result = recurrenceDateTimeTagParser.parse(
      "RDATE;VALUE=DATE-TIME:19960402T010000Z,19960403T010000Z,19960404T010000Z"
    )

    val expected = RecurrenceDateTimeTag(
      param = ValueParam(ParamValueType.DATE_TIME),
      values = listOf(
        UtcDateTime("19960402T010000Z"),
        UtcDateTime("19960403T010000Z"),
        UtcDateTime("19960404T010000Z")
      )
    )
    assertEquals(expected, result)
  }
}

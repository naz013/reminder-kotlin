package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

import com.elementary.tasks.core.utils.datetime.recurrence.Param
import com.elementary.tasks.core.utils.datetime.recurrence.ParamValueType
import com.elementary.tasks.core.utils.datetime.recurrence.ValueParam
import org.junit.Assert.assertEquals
import org.junit.Test

class ParamParserTest {

  private val paramParser = ParamParser()

  @Test
  fun testParseEmptyParam() {
    val result = paramParser.parse("")

    assertEquals(emptyList<Param>(), result)
  }

  @Test
  fun testParseWrongParam() {
    val result = paramParser.parse("RDATE;TZID=America/New_York:19970714T083000")

    assertEquals(emptyList<Param>(), result)
  }

  @Test
  fun testParseSingleParam() {
    val result = paramParser.parse("RDATE;VALUE=DATE")

    val expected = listOf(
      ValueParam(ParamValueType.DATE)
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseSingleParam_DateTime() {
    val result = paramParser.parse("RDATE;VALUE=DATE-TIME")

    val expected = listOf(
      ValueParam(ParamValueType.DATE_TIME)
    )
    assertEquals(expected, result)
  }
}

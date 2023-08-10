package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

import org.junit.Assert.assertEquals
import org.junit.Test

class LinesParserTest {

  private val linesParser = LinesParser()

  @Test
  fun testEmptyString() {
    val input = ""

    val result = linesParser.parse(input)

    assertEquals(emptyList<String>(), result)
  }

  @Test
  fun testWithBackspaceString_shouldReturnOneItem() {
    val input = "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5:\n" +
      " RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"

    val result = linesParser.parse(input)

    val expected = listOf(
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5:RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"
    )
    assertEquals(expected, result)
  }

  @Test
  fun testWithBackspaceTriLineString_shouldReturnOneItem() {
    val input = "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5:\n" +
      " RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5:\n" +
      "  RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"

    val result = linesParser.parse(input)

    val expected = listOf(
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5:RRULE:FREQ=DAILY;INTERVAL=10;" +
        "COUNT=5:RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"
    )
    assertEquals(expected, result)
  }

  @Test
  fun testWithMultiRuleString_shouldReturnTwoItems() {
    val input = "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5:\n" +
      " RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5\n" +
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"

    val result = linesParser.parse(input)

    val expected = listOf(
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5:RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5",
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"
    )
    assertEquals(expected, result)
  }
}

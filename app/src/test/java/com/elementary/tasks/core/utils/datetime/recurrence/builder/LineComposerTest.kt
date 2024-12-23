package com.elementary.tasks.core.utils.datetime.recurrence.builder

import com.github.naz013.icalendar.builder.LineComposer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LineComposerTest {

  private val lineComposer = LineComposer()

  @Test
  fun testComposingEmptyList() {
    val lines = emptyList<String>()

    val result = lineComposer.compose(lines)

    assertNull(result)
  }

  @Test
  fun testComposingSingleLineList() {
    val lines = listOf(
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"
    )

    val result = lineComposer.compose(lines)

    assertEquals("RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5", result)
  }

  @Test
  fun testComposingMultilineList() {
    val lines = listOf(
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5",
      "\nRRULE:FREQ=DAILY;INTERVAL=10;COUNT=5",
      "\n RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"
    )

    val result = lineComposer.compose(lines)

    val expected = "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5\n" +
      "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5\n" +
      " RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5"

    assertEquals(expected, result)
  }
}

package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.RepeatTag
import com.elementary.tasks.core.utils.datetime.recurrence.VersionTag
import org.junit.Assert.*
import org.junit.Test

class RepeatTagParserTest {

  private val repeatTagParser = RepeatTagParser()

  @Test
  fun testParseEmptyTag() {
    val result = repeatTagParser.parse("")
    assertNull(result)
  }

  @Test
  fun testParseWrongTag() {
    val result = repeatTagParser.parse("RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5")
    assertNull(result)
  }

  @Test
  fun testParseCorrectTag() {
    val result = repeatTagParser.parse("REPEAT:4")
    assertEquals(RepeatTag(4), result)
  }
}

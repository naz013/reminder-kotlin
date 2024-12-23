package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.github.naz013.icalendar.VersionTag
import com.github.naz013.icalendar.parser.VersionTagParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VersionTagParserTest {

  private val versionTagParser = VersionTagParser()

  @Test
  fun testParseEmptyTag() {
    val result = versionTagParser.parse("")
    assertNull(result)
  }

  @Test
  fun testParseWrongTag() {
    val result = versionTagParser.parse("RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5")
    assertNull(result)
  }

  @Test
  fun testParseCorrectTag() {
    val result = versionTagParser.parse("VERSION:2.0")
    assertEquals(VersionTag("2.0"), result)
  }
}

package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.Duration
import com.github.naz013.icalendar.DurationTag
import com.github.naz013.icalendar.parser.DurationTagParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DurationTagParserTest {

  private val durationTagParser = DurationTagParser()

  @Test
  fun testParseEmptyTag() {
    val result = durationTagParser.parse("")
    assertNull(result)
  }

  @Test
  fun testParseWrongTag() {
    val result = durationTagParser.parse("RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5")
    assertNull(result)
  }

  @Test
  fun testParseCorrectTag_onlyTime() {
    val result = durationTagParser.parse("DURATION:PT1H0M0S")

    val expected = DurationTag(
      Duration(weeks = 0, days = 0, hours = 1, minutes = 0, seconds = 0)
    )
    assertEquals(expected, result)
  }

  @Test
  fun testParseCorrectTag() {
    val result = durationTagParser.parse("DURATION:P5W3DT1H0M0S")

    val expected = DurationTag(
      Duration(weeks = 5, days = 3, hours = 1, minutes = 0, seconds = 0)
    )
    assertEquals(expected, result)
  }
}

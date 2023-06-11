package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeEndTag
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeStampTag
import com.elementary.tasks.core.utils.datetime.recurrence.DateTimeStartTag
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class DateTimeTagParserTest {

  private val dateTimeTagParser = DateTimeTagParser()

  @Test
  fun testParseEmptyTag() {
    val result = dateTimeTagParser.parse("")
    assertNull(result)
  }

  @Test
  fun testParseWrongTag() {
    val result = dateTimeTagParser.parse("RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5")
    assertNull(result)
  }

  @Test
  fun testParseDateTimeStart() {
    val result = dateTimeTagParser.parse("DTSTART:19980118T073000Z")

    val expected = ZonedDateTime.of(1998, 1, 18, 7, 30, 0, 0, UTC_ZONE)

    assert(result is DateTimeStartTag)
    assertEquals(expected.toLocalDateTime(), result?.value?.dateTime)
  }

  @Test
  fun testParseDateTimeEnd() {
    val result = dateTimeTagParser.parse("DTEND:19980118T073000Z")

    val expected = ZonedDateTime.of(1998, 1, 18, 7, 30, 0, 0, UTC_ZONE)

    assert(result is DateTimeEndTag)
    assertEquals(expected.toLocalDateTime(), result?.value?.dateTime)
  }

  @Test
  fun testParseDateTimeStamp() {
    val result = dateTimeTagParser.parse("DTSTAMP:19980118T073000Z")

    val expected = ZonedDateTime.of(1998, 1, 18, 7, 30, 0, 0, UTC_ZONE)

    assert(result is DateTimeStampTag)
    assertEquals(expected.toLocalDateTime(), result?.value?.dateTime)
  }

  @Test
  fun testParseDateTimeStampWithValueParam() {
    val result = dateTimeTagParser.parse("DTSTAMP;VALUE=DATE-TIME:19980118T073000Z")

    val expected = ZonedDateTime.of(1998, 1, 18, 7, 30, 0, 0, UTC_ZONE)

    assert(result is DateTimeStampTag)
    assertEquals(expected.toLocalDateTime(), result?.value?.dateTime)
  }

  companion object {
    private val UTC_ZONE = ZoneId.of(UtcDateTime.UTC)
  }
}

package com.github.naz013.icalendar.parser

import com.github.naz013.icalendar.DateTimeStartTag
import com.github.naz013.icalendar.ParamValueType
import com.github.naz013.icalendar.RecurrenceDateTimeTag
import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.icalendar.ValueParam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime

class TimeZoneIdTagTest {

  private val dateTimeTagParser = DateTimeTagParser()
  private val recurrenceDateTimeTagParser = RecurrenceDateTimeTagParser()

  @Test
  fun `reads TZID from DTSTART`() {
    val result = dateTimeTagParser.parse("DTSTART;TZID=Europe/Berlin:19980118T073000") as DateTimeStartTag

    assertEquals("Europe/Berlin", result.timeZoneId)
    assertEquals(LocalDateTime.of(1998, 1, 18, 7, 30, 0), result.value.dateTime)
  }

  @Test
  fun `DTSTART without TZID is floating`() {
    val result = dateTimeTagParser.parse("DTSTART:19980118T073000") as DateTimeStartTag

    assertNull(result.timeZoneId)
  }

  @Test
  fun `DTSTART in UTC with a TZID stays floating`() {
    val result = dateTimeTagParser.parse("DTSTART;TZID=Europe/Berlin:19980118T073000Z") as DateTimeStartTag

    assertNull(result.timeZoneId)
  }

  @Test
  fun `Windows zone name falls back to floating`() {
    val result = dateTimeTagParser.parse("DTSTART;TZID=W. Europe Standard Time:19980118T073000") as DateTimeStartTag

    assertNull(result.timeZoneId)
    assertEquals(LocalDateTime.of(1998, 1, 18, 7, 30, 0), result.value.dateTime)
  }

  @Test
  fun `reads TZID from RDATE next to VALUE`() {
    val result = recurrenceDateTimeTagParser.parse(
      "RDATE;VALUE=DATE-TIME;TZID=America/New_York:19980118T073000,19980119T073000"
    )

    assertEquals("America/New_York", result?.timeZoneId)
    assertEquals(ValueParam(ParamValueType.DATE_TIME), result?.param)
    assertEquals(2, result?.values?.size)
  }

  @Test
  fun `exports DTSTART with TZID when fixed`() {
    val tag = DateTimeStartTag(UtcDateTime(LocalDateTime.of(1998, 1, 18, 7, 30, 0)), "Europe/Berlin")

    assertEquals("DTSTART;TZID=Europe/Berlin:19980118T073000", tag.buildString())
  }

  @Test
  fun `exports plain DTSTART when floating`() {
    val tag = DateTimeStartTag(UtcDateTime(LocalDateTime.of(1998, 1, 18, 7, 30, 0)))

    assertEquals("DTSTART:19980118T073000", tag.buildString())
  }

  @Test
  fun `DTSTART TZID survives export and parse`() {
    val tag = DateTimeStartTag(UtcDateTime(LocalDateTime.of(2026, 7, 22, 9, 0, 0)), "Asia/Tokyo")

    val parsed = dateTimeTagParser.parse(tag.buildString()) as DateTimeStartTag

    assertEquals(tag, parsed)
  }

  @Test
  fun `exports RDATE with VALUE and TZID`() {
    val tag = RecurrenceDateTimeTag(
      ValueParam(ParamValueType.DATE_TIME),
      listOf(UtcDateTime(LocalDateTime.of(1998, 1, 18, 7, 30, 0))),
      "Europe/Berlin"
    )

    assertEquals("RDATE;VALUE=DATE-TIME;TZID=Europe/Berlin:19980118T073000", tag.buildString())
  }
}

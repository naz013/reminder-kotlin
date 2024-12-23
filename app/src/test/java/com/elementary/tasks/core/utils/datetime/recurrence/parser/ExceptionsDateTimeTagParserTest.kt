package com.elementary.tasks.core.utils.datetime.recurrence.parser

import com.github.naz013.icalendar.UtcDateTime
import com.github.naz013.icalendar.parser.ExceptionsDateTimeTagParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class ExceptionsDateTimeTagParserTest {

  private val exceptionsDateTimeTagParser = ExceptionsDateTimeTagParser()

  @Test
  fun testParseEmptyTag() {
    val result = exceptionsDateTimeTagParser.parse("")
    assertNull(result)
  }

  @Test
  fun testParseWrongTag() {
    val result = exceptionsDateTimeTagParser.parse("RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5")
    assertNull(result)
  }

  @Test
  fun testParseCorrectTag_singleElement() {
    val result = exceptionsDateTimeTagParser.parse("EXDATE:19960402T010000Z")

    val expected = ZonedDateTime.of(1996, 4, 2, 1, 0, 0, 0, UTC_ZONE)

    assertEquals(1, result?.values?.size)
    assertEquals(
      listOf(
        expected.toLocalDateTime()
      ),
      result?.values?.map { it.dateTime }
    )
  }

  @Test
  fun testParseCorrectTag_multipleElement() {
    val result = exceptionsDateTimeTagParser.parse(
      "EXDATE:19960402T010000Z,19960403T010000Z,19960404T010000Z"
    )

    val expected = listOf(
      ZonedDateTime.of(1996, 4, 2, 1, 0, 0, 0, UTC_ZONE),
      ZonedDateTime.of(1996, 4, 3, 1, 0, 0, 0, UTC_ZONE),
      ZonedDateTime.of(1996, 4, 4, 1, 0, 0, 0, UTC_ZONE)
    )

    assertEquals(expected.size, result?.values?.size)
    assertEquals(
      expected.map { it.toLocalDateTime() },
      result?.values?.map { it.dateTime }
    )
  }

  companion object {
    private val UTC_ZONE = ZoneId.of(UtcDateTime.UTC)
  }
}

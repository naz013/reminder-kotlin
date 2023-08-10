package com.elementary.tasks.core.utils.datetime.recurrence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime

class UtcDateTimeTest {

  @Test
  fun testEmptyStringParse_shouldDoNothing() {
    val utcDateTime = UtcDateTime("")

    assertNull(utcDateTime.dateTime)
  }

  @Test
  fun testWrongFormatParse_shouldDoNothing() {
    val utcDateTime = UtcDateTime("2023-05-25T15-30-00")

    assertNull(utcDateTime.dateTime)
  }

  @Test
  fun testUtcFormatParse_shouldPutCorrectLocalDateTime() {
    val nowDateTime = LocalDateTime.now()

    val utcString = UtcDateTime(nowDateTime).buildString()

    val utcDateTime = UtcDateTime(utcString)
    val expectedDateTime = LocalDateTime.of(
      nowDateTime.year,
      nowDateTime.monthValue,
      nowDateTime.dayOfMonth,
      nowDateTime.hour,
      nowDateTime.minute,
      nowDateTime.second
    )

    assertNotNull(utcDateTime.dateTime)
    assertEquals(expectedDateTime, utcDateTime.dateTime)
  }

  @Test
  fun testLocalFormatParse_shouldPutCorrectLocalDateTime() {
    val nowDateTime = LocalDateTime.now()

    val startDateTime = UtcDateTime(nowDateTime)
    val localString = startDateTime.buildString()

    val utcDateTime = UtcDateTime(localString)

    assertNotNull(utcDateTime.dateTime)
    assertEquals(nowDateTime.withNano(0), utcDateTime.dateTime)
  }
}

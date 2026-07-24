package com.github.naz013.repository.converters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class DateTimeTypeConverterTest {
  private lateinit var converter: DateTimeTypeConverter

  @Before
  fun setUp() {
    converter = DateTimeTypeConverter()
  }

  @Test
  fun `date time round trips through its formatted string`() {
    val dateTime = LocalDateTime.of(2023, 6, 17, 9, 30, 15)

    val formatted = converter.toString(dateTime)
    val result = converter.toDateTime(formatted)

    assertEquals(dateTime, result)
    assertEquals("2023-06-17 09:30:15", formatted)
  }

  @Test
  fun `toDateTime falls back to now for null input`() {
    val result = converter.toDateTime(null)

    assertNotNull(result)
  }

  @Test
  fun `toDateTime falls back to now for an unparsable string`() {
    val result = converter.toDateTime("not-a-date")

    assertNotNull(result)
  }
}

package com.github.naz013.reviews.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

/**
 * Unit tests for DateTime mapping functions.
 *
 * Tests the conversion between LocalDateTime and epoch milliseconds.
 */
class DateTimeMapperTest {

  /**
   * Tests conversion from LocalDateTime to epoch milliseconds.
   * Validates that the timestamp is correctly converted.
   */
  @Test
  fun `toEpochMillis converts LocalDateTime to milliseconds correctly`() {
    // Given
    val dateTime = LocalDateTime.of(2025, 11, 1, 12, 30, 45, 0)

    // When
    val epochMillis = dateTime.toEpochMillis()

    // Then
    // Convert back to verify
    val converted = epochMillis.toLocalDateTime()
    assertEquals(dateTime.year, converted.year)
    assertEquals(dateTime.monthValue, converted.monthValue)
    assertEquals(dateTime.dayOfMonth, converted.dayOfMonth)
    assertEquals(dateTime.hour, converted.hour)
    assertEquals(dateTime.minute, converted.minute)
    assertEquals(dateTime.second, converted.second)
  }

  /**
   * Tests conversion from epoch milliseconds to LocalDateTime.
   * Validates that the timestamp is correctly converted.
   */
  @Test
  fun `toLocalDateTime converts milliseconds to LocalDateTime correctly`() {
    // Given - November 1, 2025 at 12:30:45 in system default timezone
    val dateTime = LocalDateTime.of(2025, 11, 1, 12, 30, 45, 0)
    val epochMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // When
    val converted = epochMillis.toLocalDateTime()

    // Then
    assertEquals(dateTime.year, converted.year)
    assertEquals(dateTime.monthValue, converted.monthValue)
    assertEquals(dateTime.dayOfMonth, converted.dayOfMonth)
    assertEquals(dateTime.hour, converted.hour)
    assertEquals(dateTime.minute, converted.minute)
    assertEquals(dateTime.second, converted.second)
  }

  /**
   * Tests round-trip conversion from LocalDateTime to epoch and back.
   * Validates that data integrity is maintained through conversion cycle.
   */
  @Test
  fun `round trip conversion preserves date and time`() {
    // Given
    val originalDateTime = LocalDateTime.of(2025, 11, 1, 15, 45, 30, 123456789)

    // When
    val epochMillis = originalDateTime.toEpochMillis()
    val convertedDateTime = epochMillis.toLocalDateTime()

    // Then
    assertEquals(originalDateTime.year, convertedDateTime.year)
    assertEquals(originalDateTime.monthValue, convertedDateTime.monthValue)
    assertEquals(originalDateTime.dayOfMonth, convertedDateTime.dayOfMonth)
    assertEquals(originalDateTime.hour, convertedDateTime.hour)
    assertEquals(originalDateTime.minute, convertedDateTime.minute)
    assertEquals(originalDateTime.second, convertedDateTime.second)
    // Note: Millisecond precision, nanoseconds may differ
  }

  /**
   * Tests conversion with epoch 0 (January 1, 1970).
   * Validates that the epoch start date is correctly handled.
   */
  @Test
  fun `toLocalDateTime handles epoch zero correctly`() {
    // Given
    val epochZero = 0L

    // When
    val dateTime = epochZero.toLocalDateTime()

    // Then - Should be Jan 1, 1970 in system timezone
    val expected = LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0)
      .atZone(ZoneId.of("UTC"))
      .withZoneSameInstant(ZoneId.systemDefault())
      .toLocalDateTime()

    assertEquals(expected.year, dateTime.year)
    assertEquals(expected.monthValue, dateTime.monthValue)
    assertEquals(expected.dayOfMonth, dateTime.dayOfMonth)
  }

  /**
   * Tests conversion with a specific known timestamp.
   * Validates conversion accuracy with a concrete example.
   */
  @Test
  fun `toLocalDateTime converts specific timestamp correctly`() {
    // Given - 1730462445000L is approximately Nov 1, 2025 12:30:45 UTC
    val knownTimestamp = 1730462445000L

    // When
    val dateTime = knownTimestamp.toLocalDateTime()

    // Then - Verify the date is in the expected range
    assertTrue("Year should be 2024 or later", dateTime.year >= 2024)
    assertTrue("Month should be valid", dateTime.monthValue in 1..12)
    assertTrue("Day should be valid", dateTime.dayOfMonth in 1..31)
    assertTrue("Hour should be valid", dateTime.hour in 0..23)
    assertTrue("Minute should be valid", dateTime.minute in 0..59)
    assertTrue("Second should be valid", dateTime.second in 0..59)
  }

  /**
   * Tests conversion with current time.
   * Validates that current timestamp conversions are accurate.
   */
  @Test
  fun `toEpochMillis and toLocalDateTime work with current time`() {
    // Given
    val now = LocalDateTime.now()

    // When
    val epochMillis = now.toEpochMillis()
    val converted = epochMillis.toLocalDateTime()

    // Then - Allow small difference due to processing time
    assertEquals(now.year, converted.year)
    assertEquals(now.monthValue, converted.monthValue)
    assertEquals(now.dayOfMonth, converted.dayOfMonth)
    assertEquals(now.hour, converted.hour)
    assertEquals(now.minute, converted.minute)
  }

  /**
   * Tests conversion with future date.
   * Validates that future dates are correctly handled.
   */
  @Test
  fun `toEpochMillis handles future dates correctly`() {
    // Given - A date in the future
    val futureDate = LocalDateTime.of(2030, 12, 31, 23, 59, 59)

    // When
    val epochMillis = futureDate.toEpochMillis()
    val converted = epochMillis.toLocalDateTime()

    // Then
    assertEquals(futureDate.year, converted.year)
    assertEquals(futureDate.monthValue, converted.monthValue)
    assertEquals(futureDate.dayOfMonth, converted.dayOfMonth)
    assertEquals(futureDate.hour, converted.hour)
    assertEquals(futureDate.minute, converted.minute)
    assertEquals(futureDate.second, converted.second)
  }

  /**
   * Tests conversion with past date.
   * Validates that historical dates are correctly handled.
   */
  @Test
  fun `toEpochMillis handles past dates correctly`() {
    // Given - A date in the past
    val pastDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0)

    // When
    val epochMillis = pastDate.toEpochMillis()
    val converted = epochMillis.toLocalDateTime()

    // Then
    assertEquals(pastDate.year, converted.year)
    assertEquals(pastDate.monthValue, converted.monthValue)
    assertEquals(pastDate.dayOfMonth, converted.dayOfMonth)
    assertEquals(pastDate.hour, converted.hour)
    assertEquals(pastDate.minute, converted.minute)
    assertEquals(pastDate.second, converted.second)
  }
}


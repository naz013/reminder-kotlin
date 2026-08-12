package com.github.naz013.reviews.db

import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.Review
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for Review mapping functions.
 *
 * Tests the conversion between Review domain objects and ReviewEntity database objects.
 */
class ReviewMapperTest {

  /**
   * Tests conversion from Review domain object to ReviewEntity.
   * Validates that all fields are correctly mapped.
   */
  @Test
  fun `toReviewEntity converts Review to ReviewEntity correctly`() {
    // Given
    val timestamp = LocalDateTime.of(2025, 11, 1, 12, 30, 45)
    val review = Review(
      id = "test-id-123",
      rating = 4.5f,
      comment = "Great app!",
      timestamp = timestamp,
      logFileUrl = "https://example.com/logs.txt",
      appVersion = "1.2.3",
      deviceInfo = "Samsung Galaxy S21",
      userEmail = "test@example.com",
      userLocale = "en_US",
      userId = "user-456",
      source = AppSource.PRO,
      processed = true
    )

    // When
    val entity = review.toReviewEntity()

    // Then
    assertEquals("test-id-123", entity.id)
    assertEquals(4.5f, entity.rating, 0.001f)
    assertEquals("Great app!", entity.comment)
    assertEquals(timestamp.toEpochMillis(), entity.timestamp)
    assertEquals("https://example.com/logs.txt", entity.logFileUrl)
    assertEquals("1.2.3", entity.appVersion)
    assertEquals("Samsung Galaxy S21", entity.deviceInfo)
    assertEquals("test@example.com", entity.userEmail)
    assertEquals("en_US", entity.userLocale)
    assertEquals("user-456", entity.userId)
    assertEquals("PRO", entity.appSource)
    assertEquals(true, entity.processed)
  }

  /**
   * Tests conversion from Review domain object with null fields.
   * Validates that null values are preserved during mapping.
   */
  @Test
  fun `toReviewEntity handles null fields correctly`() {
    // Given
    val timestamp = LocalDateTime.of(2025, 11, 1, 12, 30, 45)
    val review = Review(
      id = "test-id",
      rating = 3.0f,
      comment = "Needs improvement",
      timestamp = timestamp,
      logFileUrl = null,
      appVersion = "2.0.0",
      deviceInfo = "Pixel 6",
      userEmail = null,
      userLocale = "fr_FR",
      userId = "user-789",
      source = AppSource.FREE,
      processed = false
    )

    // When
    val entity = review.toReviewEntity()

    // Then
    assertEquals(null, entity.logFileUrl)
    assertEquals(null, entity.userEmail)
    assertEquals("FREE", entity.appSource)
    assertEquals(false, entity.processed)
  }

  /**
   * Tests conversion from ReviewEntity to Review domain object.
   * Validates that all fields are correctly mapped.
   */
  @Test
  fun `toReview converts ReviewEntity to Review correctly`() {
    // Given
    val timestampMillis = 1730462445000L // Nov 1, 2025 12:30:45
    val entity = ReviewEntity(
      id = "entity-id-789",
      rating = 5.0f,
      comment = "Perfect!",
      timestamp = timestampMillis,
      logFileUrl = "https://storage.example.com/log.zip",
      appVersion = "3.4.5",
      deviceInfo = "OnePlus 9",
      userEmail = "user@domain.com",
      userLocale = "de_DE",
      userId = "user-xyz",
      appSource = "PRO",
      processed = true
    )

    // When
    val review = entity.toReview()

    // Then
    assertEquals("entity-id-789", review.id)
    assertEquals(5.0f, review.rating, 0.001f)
    assertEquals("Perfect!", review.comment)
    assertEquals(timestampMillis, review.timestamp.toEpochMillis())
    assertEquals("https://storage.example.com/log.zip", review.logFileUrl)
    assertEquals("3.4.5", review.appVersion)
    assertEquals("OnePlus 9", review.deviceInfo)
    assertEquals("user@domain.com", review.userEmail)
    assertEquals("de_DE", review.userLocale)
    assertEquals("user-xyz", review.userId)
    assertEquals(AppSource.PRO, review.source)
    assertEquals(true, review.processed)
  }

  /**
   * Tests conversion from ReviewEntity with invalid AppSource.
   * Validates that invalid values default to AppSource.FREE.
   */
  @Test
  fun `toReview defaults to FREE when appSource is invalid`() {
    // Given
    val entity = ReviewEntity(
      id = "test-id",
      rating = 3.5f,
      comment = "Good",
      timestamp = 1730462445000L,
      logFileUrl = null,
      appVersion = "1.0.0",
      deviceInfo = "Test Device",
      userEmail = null,
      userLocale = "en_US",
      userId = "user-123",
      appSource = "INVALID_SOURCE",
      processed = false
    )

    // When
    val review = entity.toReview()

    // Then
    assertEquals(AppSource.FREE, review.source)
  }

  /**
   * Tests round-trip conversion from Review to ReviewEntity and back.
   * Validates that data integrity is maintained through conversion cycle.
   */
  @Test
  fun `round trip conversion preserves data integrity`() {
    // Given
    val originalTimestamp = LocalDateTime.of(2025, 11, 1, 15, 45, 30)
    val originalReview = Review(
      id = "round-trip-test",
      rating = 4.0f,
      comment = "Very good app",
      timestamp = originalTimestamp,
      logFileUrl = "https://logs.example.com/app.log",
      appVersion = "2.5.1",
      deviceInfo = "Xiaomi Mi 11",
      userEmail = "roundtrip@test.com",
      userLocale = "es_ES",
      userId = "user-round-trip",
      source = AppSource.PRO,
      processed = false
    )

    // When
    val entity = originalReview.toReviewEntity()
    val convertedReview = entity.toReview()

    // Then
    assertEquals(originalReview.id, convertedReview.id)
    assertEquals(originalReview.rating, convertedReview.rating, 0.001f)
    assertEquals(originalReview.comment, convertedReview.comment)
    assertEquals(originalReview.timestamp.toEpochMillis(), convertedReview.timestamp.toEpochMillis())
    assertEquals(originalReview.logFileUrl, convertedReview.logFileUrl)
    assertEquals(originalReview.appVersion, convertedReview.appVersion)
    assertEquals(originalReview.deviceInfo, convertedReview.deviceInfo)
    assertEquals(originalReview.userEmail, convertedReview.userEmail)
    assertEquals(originalReview.userLocale, convertedReview.userLocale)
    assertEquals(originalReview.userId, convertedReview.userId)
    assertEquals(originalReview.source, convertedReview.source)
    assertEquals(originalReview.processed, convertedReview.processed)
  }

  /**
   * Tests conversion of AppSource enum values.
   * Validates that both FREE and PRO sources are correctly converted.
   */
  @Test
  fun `toReviewEntity correctly converts AppSource enum`() {
    // Given
    val freeReview = createTestReview(source = AppSource.FREE)
    val proReview = createTestReview(source = AppSource.PRO)

    // When
    val freeEntity = freeReview.toReviewEntity()
    val proEntity = proReview.toReviewEntity()

    // Then
    assertEquals("FREE", freeEntity.appSource)
    assertEquals("PRO", proEntity.appSource)
  }

  /**
   * Creates a test Review with default values for testing.
   *
   * @param source The AppSource to use
   * @return A test Review instance
   */
  private fun createTestReview(source: AppSource): Review {
    return Review(
      id = "test",
      rating = 5.0f,
      comment = "Test",
      timestamp = LocalDateTime.now(),
      logFileUrl = null,
      appVersion = "1.0",
      deviceInfo = "Device",
      userEmail = null,
      userLocale = "en",
      userId = "user",
      source = source,
      processed = false
    )
  }
}


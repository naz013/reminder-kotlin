package com.github.naz013.reviews.db

import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.Review
import com.github.naz013.reviews.auth.FirebaseAuthManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for ReviewRepositoryImpl.
 *
 * Tests repository operations including save, get, update, delete, and authentication handling.
 */
class ReviewRepositoryImplTest {

  private lateinit var firestoreDatabase: FirestoreDatabase
  private lateinit var authManager: FirebaseAuthManager
  private lateinit var repository: ReviewRepositoryImpl

  /**
   * Sets up test fixtures before each test.
   * Initializes mocked dependencies and repository instance.
   */
  @Before
  fun setup() {
    firestoreDatabase = mockk(relaxed = true)
    authManager = mockk(relaxed = true)
    repository = ReviewRepositoryImpl(firestoreDatabase, authManager)
  }

  // ===========================
  // Save Review Tests
  // ===========================

  /**
   * Tests saving a review successfully.
   * Validates that authentication is checked and review is saved with user ID.
   */
  @Test
  fun `saveReview saves review successfully when authenticated`() = runTest {
    // Given
    val review = createTestReview(id = "review-123")
    val userId = "user-456"

    coEvery { authManager.ensureAuthenticated() } returns Result.success(Unit)
    every { authManager.getCurrentUserId() } returns userId
    coEvery { firestoreDatabase.saveReview(any()) } returns Result.success(Unit)

    // When
    val result = repository.saveReview(review)

    // Then
    assertTrue(result.isSuccess)
    coVerify { authManager.ensureAuthenticated() }
    coVerify { firestoreDatabase.saveReview(any()) }
  }

  /**
   * Tests saving a review with blank ID.
   * Validates that input validation rejects blank IDs.
   */
  @Test
  fun `saveReview fails when review ID is blank`() = runTest {
    // Given
    val review = createTestReview(id = "")

    // When
    val result = repository.saveReview(review)

    // Then
    assertTrue(result.isFailure)
    assertNotNull(result.exceptionOrNull())
    assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    assertEquals("Review ID cannot be blank", result.exceptionOrNull()?.message)
  }

  /**
   * Tests saving a review when authentication fails.
   * Validates that authentication failure is properly handled.
   */
  @Test
  fun `saveReview fails when authentication fails`() = runTest {
    // Given
    val review = createTestReview(id = "review-123")
    val authException = Exception("Auth failed")

    coEvery { authManager.ensureAuthenticated() } returns Result.failure(authException)

    // When
    val result = repository.saveReview(review)

    // Then
    assertTrue(result.isFailure)
    assertEquals(authException, result.exceptionOrNull())
    coVerify(exactly = 0) { firestoreDatabase.saveReview(any()) }
  }

  /**
   * Tests saving a review when user ID is null after authentication.
   * Validates that null user ID is properly handled.
   */
  @Test
  fun `saveReview fails when user ID is null`() = runTest {
    // Given
    val review = createTestReview(id = "review-123")

    coEvery { authManager.ensureAuthenticated() } returns Result.success(Unit)
    every { authManager.getCurrentUserId() } returns null

    // When
    val result = repository.saveReview(review)

    // Then
    assertTrue(result.isFailure)
    assertNotNull(result.exceptionOrNull())
    assertTrue(result.exceptionOrNull() is IllegalStateException)
    assertEquals("User ID is null", result.exceptionOrNull()?.message)
  }

  // ===========================
  // Get Review Tests
  // ===========================

  /**
   * Tests retrieving a review by ID successfully.
   * Validates that authentication is checked and review is retrieved.
   */
  @Test
  fun `getReview retrieves review successfully when authenticated`() = runTest {
    // Given
    val reviewId = "review-123"
    val reviewEntity = createTestReviewEntity(id = reviewId)

    coEvery { authManager.ensureAuthenticated() } returns Result.success(Unit)
    coEvery { firestoreDatabase.readReview(reviewId) } returns Result.success(reviewEntity)

    // When
    val result = repository.getReview(reviewId)

    // Then
    assertTrue(result.isSuccess)
    val review = result.getOrNull()
    assertNotNull(review)
    assertEquals(reviewId, review?.id)
    coVerify { authManager.ensureAuthenticated() }
    coVerify { firestoreDatabase.readReview(reviewId) }
  }

  /**
   * Tests retrieving a review with blank ID.
   * Validates that input validation rejects blank IDs.
   */
  @Test
  fun `getReview fails when review ID is blank`() = runTest {
    // Given
    val reviewId = ""

    // When
    val result = repository.getReview(reviewId)

    // Then
    assertTrue(result.isFailure)
    assertNotNull(result.exceptionOrNull())
    assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    assertEquals("Review ID cannot be blank", result.exceptionOrNull()?.message)
  }

  /**
   * Tests retrieving a review when authentication fails.
   * Validates that authentication failure is properly handled.
   */
  @Test
  fun `getReview fails when authentication fails`() = runTest {
    // Given
    val reviewId = "review-123"
    val authException = Exception("Auth failed")

    coEvery { authManager.ensureAuthenticated() } returns Result.failure(authException)

    // When
    val result = repository.getReview(reviewId)

    // Then
    assertTrue(result.isFailure)
    assertEquals(authException, result.exceptionOrNull())
    coVerify(exactly = 0) { firestoreDatabase.readReview(any()) }
  }

  // ===========================
  // Get All Reviews Tests
  // ===========================

  /**
   * Tests retrieving all reviews successfully.
   * Validates that authentication is checked and all reviews are retrieved.
   */
  @Test
  fun `getAllReviews retrieves all reviews successfully when authenticated`() = runTest {
    // Given
    val reviewEntities = listOf(
      createTestReviewEntity(id = "review-1"),
      createTestReviewEntity(id = "review-2"),
      createTestReviewEntity(id = "review-3")
    )

    coEvery { authManager.ensureAuthenticated() } returns Result.success(Unit)
    coEvery { firestoreDatabase.readAllReviews() } returns Result.success(reviewEntities)

    // When
    val result = repository.getAllReviews()

    // Then
    assertTrue(result.isSuccess)
    val reviews = result.getOrNull()
    assertNotNull(reviews)
    assertEquals(3, reviews?.size)
    coVerify { authManager.ensureAuthenticated() }
    coVerify { firestoreDatabase.readAllReviews() }
  }

  /**
   * Tests retrieving all reviews when list is empty.
   * Validates that empty list is properly handled.
   */
  @Test
  fun `getAllReviews returns empty list when no reviews exist`() = runTest {
    // Given
    coEvery { authManager.ensureAuthenticated() } returns Result.success(Unit)
    coEvery { firestoreDatabase.readAllReviews() } returns Result.success(emptyList())

    // When
    val result = repository.getAllReviews()

    // Then
    assertTrue(result.isSuccess)
    val reviews = result.getOrNull()
    assertNotNull(reviews)
    assertTrue(reviews?.isEmpty() == true)
  }

  // ===========================
  // Delete Review Tests
  // ===========================

  /**
   * Tests deleting a review successfully.
   * Validates that authentication is checked and review is deleted.
   */
  @Test
  fun `deleteReview deletes review successfully when authenticated`() = runTest {
    // Given
    val reviewId = "review-to-delete"

    coEvery { authManager.ensureAuthenticated() } returns Result.success(Unit)
    coEvery { firestoreDatabase.deleteReview(reviewId) } returns Result.success(Unit)

    // When
    val result = repository.deleteReview(reviewId)

    // Then
    assertTrue(result.isSuccess)
    coVerify { authManager.ensureAuthenticated() }
    coVerify { firestoreDatabase.deleteReview(reviewId) }
  }

  /**
   * Tests deleting a review with blank ID.
   * Validates that input validation rejects blank IDs.
   */
  @Test
  fun `deleteReview fails when review ID is blank`() = runTest {
    // Given
    val reviewId = ""

    // When
    val result = repository.deleteReview(reviewId)

    // Then
    assertTrue(result.isFailure)
    assertNotNull(result.exceptionOrNull())
    assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    assertEquals("Review ID cannot be blank", result.exceptionOrNull()?.message)
  }

  // ===========================
  // Update Review Tests
  // ===========================

  /**
   * Tests updating a review successfully.
   * Validates that authentication is checked and review is updated.
   */
  @Test
  fun `updateReview updates review successfully when authenticated`() = runTest {
    // Given
    val review = createTestReview(id = "review-123", processed = true)

    coEvery { authManager.ensureAuthenticated() } returns Result.success(Unit)
    coEvery { firestoreDatabase.updateReview(any()) } returns Result.success(Unit)

    // When
    val result = repository.updateReview(review)

    // Then
    assertTrue(result.isSuccess)
    coVerify { authManager.ensureAuthenticated() }
    coVerify { firestoreDatabase.updateReview(any()) }
  }

  /**
   * Tests updating a review with blank ID.
   * Validates that input validation rejects blank IDs.
   */
  @Test
  fun `updateReview fails when review ID is blank`() = runTest {
    // Given
    val review = createTestReview(id = "")

    // When
    val result = repository.updateReview(review)

    // Then
    assertTrue(result.isFailure)
    assertNotNull(result.exceptionOrNull())
    assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    assertEquals("Review ID cannot be blank", result.exceptionOrNull()?.message)
  }

  // ===========================
  // Helper Methods
  // ===========================

  /**
   * Creates a test Review instance with default or specified values.
   *
   * @param id Review ID
   * @param userId User ID
   * @param processed Whether the review has been processed
   * @return A test Review instance
   */
  private fun createTestReview(
    id: String = "test-id",
    userId: String = "test-user",
    processed: Boolean = false
  ): Review {
    return Review(
      id = id,
      rating = 4.5f,
      comment = "Test comment",
      timestamp = LocalDateTime.of(2025, 11, 1, 12, 0, 0),
      logFileUrl = "https://example.com/log.txt",
      appVersion = "1.0.0",
      deviceInfo = "Test Device",
      userEmail = "test@example.com",
      userLocale = "en_US",
      userId = userId,
      source = AppSource.FREE,
      processed = processed
    )
  }

  /**
   * Creates a test ReviewEntity instance with default or specified values.
   *
   * @param id Review ID
   * @return A test ReviewEntity instance
   */
  private fun createTestReviewEntity(id: String = "test-id"): ReviewEntity {
    return ReviewEntity(
      id = id,
      rating = 4.5f,
      comment = "Test comment",
      timestamp = 1730462400000L,
      logFileUrl = "https://example.com/log.txt",
      appVersion = "1.0.0",
      deviceInfo = "Test Device",
      userEmail = "test@example.com",
      userLocale = "en_US",
      userId = "test-user",
      appSource = "FREE",
      processed = false
    )
  }
}


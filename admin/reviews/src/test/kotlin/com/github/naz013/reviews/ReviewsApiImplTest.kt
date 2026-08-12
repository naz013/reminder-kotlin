package com.github.naz013.reviews

import com.github.naz013.reviews.auth.FirebaseAuthManager
import com.github.naz013.reviews.db.ReviewRepositoryImpl
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Unit tests for ReviewsApiImpl.
 *
 * Tests the API implementation including review operations and authentication methods.
 */
class ReviewsApiImplTest {

  private lateinit var reviewRepository: ReviewRepositoryImpl
  private lateinit var authManager: FirebaseAuthManager
  private lateinit var reviewsApi: ReviewsApiImpl

  /**
   * Sets up test fixtures before each test.
   * Initializes mocked dependencies and API instance.
   */
  @Before
  fun setup() {
    reviewRepository = mockk(relaxed = true)
    authManager = mockk(relaxed = true)
    reviewsApi = ReviewsApiImpl(reviewRepository, authManager)
  }

  // ===========================
  // Get All Reviews Tests
  // ===========================

  /**
   * Tests retrieving all reviews successfully.
   * Validates that repository method is called and result is returned.
   */
  @Test
  fun `getAllReviews returns success with list of reviews`() = runTest {
    // Given
    val expectedReviews = listOf(
      createTestReview(id = "review-1"),
      createTestReview(id = "review-2")
    )
    coEvery { reviewRepository.getAllReviews() } returns Result.success(expectedReviews)

    // When
    val result = reviewsApi.getAllReviews()

    // Then
    assertTrue(result.isSuccess)
    val reviews = result.getOrNull()
    assertNotNull(reviews)
    assertEquals(2, reviews?.size)
    coVerify { reviewRepository.getAllReviews() }
  }

  /**
   * Tests retrieving all reviews when repository returns failure.
   * Validates that failure is properly propagated.
   */
  @Test
  fun `getAllReviews returns failure when repository fails`() = runTest {
    // Given
    val exception = Exception("Database error")
    coEvery { reviewRepository.getAllReviews() } returns Result.failure(exception)

    // When
    val result = reviewsApi.getAllReviews()

    // Then
    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  // ===========================
  // Get Review Tests
  // ===========================

  /**
   * Tests retrieving a single review by ID successfully.
   * Validates that repository method is called with correct ID.
   */
  @Test
  fun `getReview returns success with review`() = runTest {
    // Given
    val reviewId = "review-123"
    val expectedReview = createTestReview(id = reviewId)
    coEvery { reviewRepository.getReview(reviewId) } returns Result.success(expectedReview)

    // When
    val result = reviewsApi.getReview(reviewId)

    // Then
    assertTrue(result.isSuccess)
    val review = result.getOrNull()
    assertNotNull(review)
    assertEquals(reviewId, review?.id)
    coVerify { reviewRepository.getReview(reviewId) }
  }

  /**
   * Tests retrieving a review when repository returns failure.
   * Validates that failure is properly propagated.
   */
  @Test
  fun `getReview returns failure when repository fails`() = runTest {
    // Given
    val reviewId = "review-123"
    val exception = Exception("Review not found")
    coEvery { reviewRepository.getReview(reviewId) } returns Result.failure(exception)

    // When
    val result = reviewsApi.getReview(reviewId)

    // Then
    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  // ===========================
  // Delete Review Tests
  // ===========================

  /**
   * Tests deleting a review successfully.
   * Validates that repository method is called with correct ID.
   */
  @Test
  fun `deleteReview returns success when repository succeeds`() = runTest {
    // Given
    val reviewId = "review-to-delete"
    coEvery { reviewRepository.deleteReview(reviewId) } returns Result.success(Unit)

    // When
    val result = reviewsApi.deleteReview(reviewId)

    // Then
    assertTrue(result.isSuccess)
    coVerify { reviewRepository.deleteReview(reviewId) }
  }

  /**
   * Tests deleting a review when repository returns failure.
   * Validates that failure is properly propagated.
   */
  @Test
  fun `deleteReview returns failure when repository fails`() = runTest {
    // Given
    val reviewId = "review-123"
    val exception = Exception("Delete failed")
    coEvery { reviewRepository.deleteReview(reviewId) } returns Result.failure(exception)

    // When
    val result = reviewsApi.deleteReview(reviewId)

    // Then
    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  // ===========================
  // Update Review Tests
  // ===========================

  /**
   * Tests updating a review successfully.
   * Validates that repository method is called with correct review.
   */
  @Test
  fun `updateReview returns success when repository succeeds`() = runTest {
    // Given
    val review = createTestReview(id = "review-123", processed = true)
    coEvery { reviewRepository.updateReview(review) } returns Result.success(Unit)

    // When
    val result = reviewsApi.updateReview(review)

    // Then
    assertTrue(result.isSuccess)
    coVerify { reviewRepository.updateReview(review) }
  }

  /**
   * Tests updating a review when repository returns failure.
   * Validates that failure is properly propagated.
   */
  @Test
  fun `updateReview returns failure when repository fails`() = runTest {
    // Given
    val review = createTestReview(id = "review-123")
    val exception = Exception("Update failed")
    coEvery { reviewRepository.updateReview(review) } returns Result.failure(exception)

    // When
    val result = reviewsApi.updateReview(review)

    // Then
    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  // ===========================
  // Download Log File Tests
  // ===========================

  /**
   * Tests downloading log file successfully.
   * Note: This test verifies the basic error handling structure.
   * Actual network testing would require more sophisticated mocking.
   */
  @Test
  fun `downloadLogFile returns failure with invalid URL`() = runTest {
    // Given
    val invalidUrl = "invalid-url"

    // When
    val result = reviewsApi.downloadLogFile(invalidUrl)

    // Then
    assertTrue(result.isFailure)
    assertNotNull(result.exceptionOrNull())
  }

  // ===========================
  // Authentication Tests
  // ===========================

  /**
   * Tests anonymous sign-in successfully.
   * Validates that auth manager method is called and result is returned.
   */
  @Test
  fun `signInAnonymously returns success with user`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser> {
      every { uid } returns "test-user-id"
    }
    coEvery { authManager.signInAnonymously() } returns Result.success(mockUser)

    // When
    val result = reviewsApi.signInAnonymously()

    // Then
    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
    coVerify { authManager.signInAnonymously() }
  }

  /**
   * Tests anonymous sign-in failure.
   * Validates that failure is properly propagated.
   */
  @Test
  fun `signInAnonymously returns failure when auth fails`() = runTest {
    // Given
    val exception = Exception("Auth failed")
    coEvery { authManager.signInAnonymously() } returns Result.failure(exception)

    // When
    val result = reviewsApi.signInAnonymously()

    // Then
    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  /**
   * Tests getting current user when user is signed in.
   * Validates that auth manager method is called and user is returned.
   */
  @Test
  fun `getCurrentUser returns user when signed in`() {
    // Given
    val mockUser = mockk<FirebaseUser> {
      every { uid } returns "current-user-id"
    }
    every { authManager.getCurrentUser() } returns mockUser

    // When
    val user = reviewsApi.getCurrentUser()

    // Then
    assertNotNull(user)
    assertEquals(mockUser, user)
    verify { authManager.getCurrentUser() }
  }

  /**
   * Tests getting current user when not signed in.
   * Validates that null is returned.
   */
  @Test
  fun `getCurrentUser returns null when not signed in`() {
    // Given
    every { authManager.getCurrentUser() } returns null

    // When
    val user = reviewsApi.getCurrentUser()

    // Then
    assertNull(user)
  }

  /**
   * Tests checking if user is signed in.
   * Validates that auth manager method is called and returns true.
   */
  @Test
  fun `isSignedIn returns true when user is authenticated`() {
    // Given
    every { authManager.isSignedIn() } returns true

    // When
    val signedIn = reviewsApi.isSignedIn()

    // Then
    assertTrue(signedIn)
    verify { authManager.isSignedIn() }
  }

  /**
   * Tests checking if user is signed in when not authenticated.
   * Validates that auth manager method is called and returns false.
   */
  @Test
  fun `isSignedIn returns false when not authenticated`() {
    // Given
    every { authManager.isSignedIn() } returns false

    // When
    val signedIn = reviewsApi.isSignedIn()

    // Then
    assertFalse(signedIn)
  }

  /**
   * Tests signing out.
   * Validates that auth manager signOut method is called.
   */
  @Test
  fun `signOut calls auth manager signOut`() {
    // Given
    every { authManager.signOut() } returns Unit

    // When
    reviewsApi.signOut()

    // Then
    verify { authManager.signOut() }
  }

  // ===========================
  // Helper Methods
  // ===========================

  /**
   * Creates a test Review instance with default or specified values.
   *
   * @param id Review ID
   * @param processed Whether the review has been processed
   * @return A test Review instance
   */
  private fun createTestReview(
    id: String = "test-id",
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
      userId = "test-user",
      source = AppSource.FREE,
      processed = processed
    )
  }
}


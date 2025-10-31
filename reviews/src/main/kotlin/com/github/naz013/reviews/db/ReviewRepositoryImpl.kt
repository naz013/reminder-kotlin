package com.github.naz013.reviews.db

import com.github.naz013.logging.Logger
import com.github.naz013.reviews.Review
import com.github.naz013.reviews.auth.FirebaseAuthManager

/**
 * Repository implementation for managing Review objects with Firestore.
 * Uses dedicated Firebase project for consolidated reviews storage across app variants.
 *
 * @property firestoreDatabase The Firestore database instance for data operations
 * @property authManager The Firebase authentication manager for user authentication
 */
internal class ReviewRepositoryImpl(
  private val firestoreDatabase: FirestoreDatabase,
  private val authManager: FirebaseAuthManager
) {

  /**
   * Saves a review to Firestore.
   * Ensures user is authenticated before saving.
   *
   * @param review The review to save
   * @return Result indicating success or failure
   */
  suspend fun saveReview(review: Review): Result<Unit> {
    // Input validation
    if (review.id.isBlank()) {
      Logger.w("ReviewRepositoryImpl", "Cannot save review with blank ID")
      return Result.failure(IllegalArgumentException("Review ID cannot be blank"))
    }

    // Ensure user is authenticated
    val authResult = authManager.ensureAuthenticated()
    if (authResult.isFailure) {
      Logger.e("ReviewRepositoryImpl", "Authentication failed before saving review")
      return Result.failure(
        authResult.exceptionOrNull() ?: Exception("Authentication failed")
      )
    }

    // Add userId to the review
    val userId = authManager.getCurrentUserId()
    if (userId == null) {
      Logger.e("ReviewRepositoryImpl", "User ID is null after authentication")
      return Result.failure(IllegalStateException("User ID is null"))
    }

    val reviewWithUserId = review.copy(userId = userId)
    return firestoreDatabase.saveReview(reviewWithUserId.toReviewEntity())
  }

  /**
   * Retrieves a single review by ID from Firestore.
   * Ensures user is authenticated before reading.
   *
   * @param reviewId The ID of the review to retrieve
   * @return Result containing the Review if found, or failure if not found
   */
  suspend fun getReview(reviewId: String): Result<Review> {
    // Input validation
    if (reviewId.isBlank()) {
      Logger.w("ReviewRepositoryImpl", "Cannot get review with blank ID")
      return Result.failure(IllegalArgumentException("Review ID cannot be blank"))
    }

    // Ensure user is authenticated
    val authResult = authManager.ensureAuthenticated()
    if (authResult.isFailure) {
      Logger.e("ReviewRepositoryImpl", "Authentication failed before reading review")
      return Result.failure(
        authResult.exceptionOrNull() ?: Exception("Authentication failed")
      )
    }

    return firestoreDatabase.readReview(reviewId).map { it.toReview() }
  }

  /**
   * Retrieves all reviews from Firestore.
   * Ensures user is authenticated before reading.
   *
   * @return Result containing a list of all reviews, or failure if the operation fails
   */
  suspend fun getAllReviews(): Result<List<Review>> {
    // Ensure user is authenticated
    val authResult = authManager.ensureAuthenticated()
    if (authResult.isFailure) {
      Logger.e("ReviewRepositoryImpl", "Authentication failed before reading all reviews")
      return Result.failure(
        authResult.exceptionOrNull() ?: Exception("Authentication failed")
      )
    }

    return firestoreDatabase.readAllReviews().map { entities ->
      entities.map { it.toReview() }
    }
  }

  /**
   * Deletes a review from Firestore by ID.
   * Ensures user is authenticated before deleting.
   *
   * @param reviewId The ID of the review to delete
   * @return Result indicating success or failure
   */
  suspend fun deleteReview(reviewId: String): Result<Unit> {
    // Input validation
    if (reviewId.isBlank()) {
      Logger.w("ReviewRepositoryImpl", "Cannot delete review with blank ID")
      return Result.failure(IllegalArgumentException("Review ID cannot be blank"))
    }

    // Ensure user is authenticated
    val authResult = authManager.ensureAuthenticated()
    if (authResult.isFailure) {
      Logger.e("ReviewRepositoryImpl", "Authentication failed before deleting review")
      return Result.failure(
        authResult.exceptionOrNull() ?: Exception("Authentication failed")
      )
    }

    return firestoreDatabase.deleteReview(reviewId)
  }
}

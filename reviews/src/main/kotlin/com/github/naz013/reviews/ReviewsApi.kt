package com.github.naz013.reviews

import android.content.Context
import com.google.firebase.auth.FirebaseUser

interface ReviewsApi {
  fun showFeedbackForm(context: Context, title: String?, appSource: AppSource)

  /**
   * Retrieves all reviews from Firestore.
   *
   * @return Result containing a list of all reviews, or failure if the operation fails
   */
  suspend fun getAllReviews(): Result<List<Review>>

  /**
   * Retrieves a single review by ID from Firestore.
   *
   * @param reviewId The ID of the review to retrieve
   * @return Result containing the Review if found, or failure if not found
   */
  suspend fun getReview(reviewId: String): Result<Review>

  /**
   * Deletes a review from Firestore by ID.
   *
   * @param reviewId The ID of the review to delete
   * @return Result indicating success or failure
   */
  suspend fun deleteReview(reviewId: String): Result<Unit>

  /**
   * Updates an existing review in Firestore.
   *
   * @param review The review to update
   * @return Result indicating success or failure
   */
  suspend fun updateReview(review: Review): Result<Unit>

  /**
   * Downloads a log file from the given URL.
   *
   * @param url The URL of the log file to download
   * @return Result containing the downloaded file bytes, or failure if download fails
   */
  suspend fun downloadLogFile(url: String): Result<ByteArray>

  /**
   * Signs in a user anonymously if not already signed in.
   * This is required for admin access to the reviews database.
   *
   * @return Result containing the authenticated user or failure
   */
  suspend fun signInAnonymously(): Result<FirebaseUser>

  /**
   * Gets the current authenticated user.
   *
   * @return The current FirebaseUser if signed in, null otherwise
   */
  fun getCurrentUser(): FirebaseUser?

  /**
   * Checks if a user is currently signed in.
   *
   * @return true if a user is signed in, false otherwise
   */
  fun isSignedIn(): Boolean

  /**
   * Signs out the current user.
   */
  fun signOut()
}

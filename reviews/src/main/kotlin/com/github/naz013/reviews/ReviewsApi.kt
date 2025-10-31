package com.github.naz013.reviews

import android.content.Context

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
}

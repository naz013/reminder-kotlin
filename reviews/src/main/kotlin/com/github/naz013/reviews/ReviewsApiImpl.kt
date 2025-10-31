package com.github.naz013.reviews

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.db.ReviewRepositoryImpl
import com.github.naz013.reviews.form.ReviewDialog

/**
 * Implementation of ReviewsSdk that manages review operations with Firestore.
 *
 * @property reviewRepositoryImpl The repository for review data operations
 */
internal class ReviewsApiImpl(
  private val reviewRepositoryImpl: ReviewRepositoryImpl,
) : ReviewsApi {

  override fun showFeedbackForm(
    context: Context,
    title: String?,
    appSource: AppSource,
  ) {
    Logger.i(TAG, "Showing feedback form")
    val dialog = ReviewDialog.newInstance(
      title = title,
      appSource = appSource
    )
    if (context is Fragment) {
      dialog.show(context.childFragmentManager, ReviewDialog.TAG)
    } else if (context is FragmentActivity) {
      dialog.show(context.supportFragmentManager, ReviewDialog.TAG)
    }
  }

  override suspend fun getAllReviews(): Result<List<Review>> {
    return reviewRepositoryImpl.getAllReviews()
  }

  override suspend fun getReview(reviewId: String): Result<Review> {
    return reviewRepositoryImpl.getReview(reviewId)
  }

  override suspend fun deleteReview(reviewId: String): Result<Unit> {
    return reviewRepositoryImpl.deleteReview(reviewId)
  }

  companion object {
    private const val TAG = "ReviewsSdk"
  }
}

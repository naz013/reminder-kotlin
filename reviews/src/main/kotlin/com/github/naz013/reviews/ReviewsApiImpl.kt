package com.github.naz013.reviews

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.auth.FirebaseAuthManager
import com.github.naz013.reviews.db.ReviewRepositoryImpl
import com.github.naz013.reviews.form.ReviewDialog
import com.google.firebase.auth.FirebaseUser

/**
 * Implementation of ReviewsSdk that manages review operations with Firestore.
 *
 * @property reviewRepositoryImpl The repository for review data operations
 * @property authManager The Firebase authentication manager
 */
internal class ReviewsApiImpl(
  private val reviewRepositoryImpl: ReviewRepositoryImpl,
  private val authManager: FirebaseAuthManager
) : ReviewsApi {

  override fun showFeedbackForm(
    context: Context,
    title: String?,
    appSource: AppSource,
    allowLogsAttachment: Boolean
  ) {
    val startTime = System.currentTimeMillis()
    Logger.i(TAG, "Showing feedback form at $startTime")
    val dialog = ReviewDialog.newInstance(
      title = title,
      appSource = appSource,
      allowLogsAttachment = allowLogsAttachment
    )
    Logger.d(TAG, "Dialog instance created in ${System.currentTimeMillis() - startTime}ms")
    if (context is FragmentActivity) {
      dialog.show(context.supportFragmentManager, ReviewDialog.TAG)
    } else {
      Logger.w(TAG, "Context is not a FragmentActivity, cannot show dialog")
    }
    Logger.d(TAG, "Dialog show() called in ${System.currentTimeMillis() - startTime}ms")
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

  override suspend fun updateReview(review: Review): Result<Unit> {
    return reviewRepositoryImpl.updateReview(review)
  }

  override suspend fun downloadLogFile(url: String): Result<ByteArray> {
    return try {
      val connection = java.net.URL(url).openConnection()
      connection.connect()
      val inputStream = connection.getInputStream()
      val bytes = inputStream.readBytes()
      inputStream.close()
      Result.success(bytes)
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to download log file from: $url", e)
      Result.failure(e)
    }
  }

  override suspend fun signInAnonymously(): Result<FirebaseUser> {
    return authManager.signInAnonymously()
  }

  override fun getCurrentUser(): FirebaseUser? {
    return authManager.getCurrentUser()
  }

  override fun isSignedIn(): Boolean {
    return authManager.isSignedIn()
  }

  override fun signOut() {
    authManager.signOut()
  }

  companion object {
    private const val TAG = "ReviewsSdk"
  }
}

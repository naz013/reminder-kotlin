package com.github.naz013.reviews.db

import com.github.naz013.logging.Logger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Database class for managing ReviewEntity objects in Firestore.
 * Uses the default Firestore bucket (root collection).
 *
 * @property firestore The Firestore instance
 */
internal class FirestoreDatabase(
  private val firestore: FirebaseFirestore
) {
  private companion object {
    private const val REVIEWS_COLLECTION = "reviews"
  }

  /**
   * Saves a ReviewEntity to Firestore.
   *
   * @param reviewEntity The review entity to save
   * @return Result indicating success or failure with exception details
   */
  suspend fun saveReview(reviewEntity: ReviewEntity): Result<Unit> {
    return try {
      // Early return for empty ID
      if (reviewEntity.id.isBlank()) {
        val error = IllegalArgumentException("Review ID cannot be blank")
        Logger.e("FirestoreDatabase", "Failed to save review: empty ID", error)
        return Result.failure(error)
      }

      firestore
        .collection(REVIEWS_COLLECTION)
        .document(reviewEntity.id)
        .set(reviewEntity)
        .await()

      Logger.i("FirestoreDatabase", "Successfully saved review with ID: ${reviewEntity.id}")
      Result.success(Unit)
    } catch (e: Exception) {
      Logger.e("FirestoreDatabase", "Failed to save review with ID: ${reviewEntity.id}", e)
      Result.failure(e)
    }
  }

  /**
   * Reads a ReviewEntity from Firestore by ID.
   *
   * @param reviewId The ID of the review to retrieve
   * @return Result containing the ReviewEntity if found, or failure with exception details
   */
  suspend fun readReview(reviewId: String): Result<ReviewEntity> {
    return try {
      // Early return for empty ID
      if (reviewId.isBlank()) {
        val error = IllegalArgumentException("Review ID cannot be blank")
        Logger.e("FirestoreDatabase", "Failed to read review: empty ID", error)
        return Result.failure(error)
      }

      val documentSnapshot = firestore
        .collection(REVIEWS_COLLECTION)
        .document(reviewId)
        .get()
        .await()

      // Early return if document doesn't exist
      if (!documentSnapshot.exists()) {
        val error = NoSuchElementException("Review with ID $reviewId not found")
        Logger.w("FirestoreDatabase", "Review not found: $reviewId")
        return Result.failure(error)
      }

      val reviewEntity = documentSnapshot.toObject(ReviewEntity::class.java)

      // Early return if deserialization failed
      if (reviewEntity == null) {
        val error = IllegalStateException("Failed to deserialize review with ID: $reviewId")
        Logger.e("FirestoreDatabase", error.message ?: "Deserialization error")
        return Result.failure(error)
      }

      Logger.i("FirestoreDatabase", "Successfully read review with ID: $reviewId")
      Result.success(reviewEntity)
    } catch (e: Exception) {
      Logger.e("FirestoreDatabase", "Failed to read review with ID: $reviewId", e)
      Result.failure(e)
    }
  }

  /**
   * Reads all ReviewEntity objects from Firestore.
   *
   * @return Result containing a list of all ReviewEntity objects, or failure with exception details
   */
  suspend fun readAllReviews(): Result<List<ReviewEntity>> {
    return try {
      val querySnapshot = firestore
        .collection(REVIEWS_COLLECTION)
        .get()
        .await()

      val reviewsList = querySnapshot.documents.mapNotNull { document ->
        try {
          document.toObject(ReviewEntity::class.java)
        } catch (e: Exception) {
          Logger.e("FirestoreDatabase", "Failed to deserialize review: ${document.id}", e)
          null
        }
      }

      Logger.i("FirestoreDatabase", "Successfully read ${reviewsList.size} reviews")
      Result.success(reviewsList)
    } catch (e: Exception) {
      Logger.e("FirestoreDatabase", "Failed to read all reviews", e)
      Result.failure(e)
    }
  }

  /**
   * Deletes a ReviewEntity from Firestore by ID.
   *
   * @param reviewId The ID of the review to delete
   * @return Result indicating success or failure with exception details
   */
  suspend fun deleteReview(reviewId: String): Result<Unit> {
    return try {
      // Early return for empty ID
      if (reviewId.isBlank()) {
        val error = IllegalArgumentException("Review ID cannot be blank")
        Logger.e("FirestoreDatabase", "Failed to delete review: empty ID", error)
        return Result.failure(error)
      }

      firestore
        .collection(REVIEWS_COLLECTION)
        .document(reviewId)
        .delete()
        .await()

      Logger.i("FirestoreDatabase", "Successfully deleted review with ID: $reviewId")
      Result.success(Unit)
    } catch (e: Exception) {
      Logger.e("FirestoreDatabase", "Failed to delete review with ID: $reviewId", e)
      Result.failure(e)
    }
  }
}

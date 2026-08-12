package com.github.nsy.reviewsadmin.ui.reviewdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.reviews.Review
import com.github.naz013.reviews.ReviewsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing review detail state and operations.
 */
class ReviewDetailViewModel(
  private val reviewsApi: ReviewsApi
) : ViewModel() {

  private val _uiState = MutableStateFlow<ReviewDetailUiState>(ReviewDetailUiState.Idle)
  val uiState: StateFlow<ReviewDetailUiState> = _uiState.asStateFlow()

  private val _currentReview = MutableStateFlow<Review?>(null)
  val currentReview: StateFlow<Review?> = _currentReview.asStateFlow()

  /**
   * Initializes the ViewModel with a review.
   *
   * @param review The review to display
   */
  fun setReview(review: Review) {
    _currentReview.value = review
    _uiState.value = ReviewDetailUiState.Idle
  }

  /**
   * Refreshes the review data from Firestore.
   *
   * @param reviewId The ID of the review to refresh
   */
  fun refreshReview(reviewId: String) {
    viewModelScope.launch {
      try {
        val result = reviewsApi.getReview(reviewId)
        if (result.isSuccess) {
          val refreshedReview = result.getOrNull()
          if (refreshedReview != null) {
            _currentReview.value = refreshedReview
          }
        } else {
          _uiState.value = ReviewDetailUiState.Error(
            "Failed to refresh review: ${result.exceptionOrNull()?.message}"
          )
        }
      } catch (e: Exception) {
        _uiState.value = ReviewDetailUiState.Error("Failed to refresh review: ${e.message}")
      }
    }
  }

  /**
   * Toggles the processed flag of a review and saves it.
   *
   * @param review The review to update
   */
  fun toggleProcessed(review: Review) {
    viewModelScope.launch {
      try {
        val updatedReview = review.copy(processed = !review.processed)
        val result = reviewsApi.updateReview(updatedReview)
        if (result.isFailure) {
          _uiState.value = ReviewDetailUiState.Error(
            "Failed to update review: ${result.exceptionOrNull()?.message}"
          )
        } else {
          // Update the current review state
          _currentReview.value = updatedReview
          _uiState.value = ReviewDetailUiState.Idle
        }
      } catch (e: Exception) {
        _uiState.value = ReviewDetailUiState.Error("Failed to update review: ${e.message}")
      }
    }
  }

  /**
   * Deletes a review from the repository.
   *
   * @param review The review to delete
   */
  fun deleteReview(review: Review) {
    viewModelScope.launch {
      try {
        val result = reviewsApi.deleteReview(review.id)
        if (result.isFailure) {
          _uiState.value = ReviewDetailUiState.Error(
            "Failed to delete review: ${result.exceptionOrNull()?.message}"
          )
        } else {
          _uiState.value = ReviewDetailUiState.Deleted
        }
      } catch (e: Exception) {
        _uiState.value = ReviewDetailUiState.Error("Failed to delete review: ${e.message}")
      }
    }
  }
}

/**
 * UI state for the review detail screen.
 */
sealed class ReviewDetailUiState {
  /**
   * Initial idle state.
   */
  data object Idle : ReviewDetailUiState()

  /**
   * Review was successfully deleted.
   */
  data object Deleted : ReviewDetailUiState()

  /**
   * Error occurred during an operation.
   *
   * @property message Error message to display
   */
  data class Error(val message: String) : ReviewDetailUiState()
}

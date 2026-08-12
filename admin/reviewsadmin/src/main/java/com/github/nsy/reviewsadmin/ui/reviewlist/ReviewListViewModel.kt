package com.github.nsy.reviewsadmin.ui.reviewlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.Review
import com.github.naz013.reviews.ReviewsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the review list screen.
 *
 * Manages loading reviews for a specific app source and provides sorting capabilities.
 *
 * @property reviewsApi API for accessing review data
 * @property dispatcherProvider Provides coroutine dispatchers
 */
class ReviewListViewModel(
  private val reviewsApi: ReviewsApi,
  private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

  private val _uiState = MutableStateFlow<ReviewListUiState>(ReviewListUiState.Loading)
  val uiState: StateFlow<ReviewListUiState> = _uiState.asStateFlow()

  private var allReviews: List<Review> = emptyList()
  private var currentAppSource: AppSource? = null
  private var currentSortColumn: SortColumn = SortColumn.TIMESTAMP
  private var currentSortOrder: SortOrder = SortOrder.DESCENDING

  /**
   * Loads reviews for the specified app source.
   *
   * @param appSource The app source to filter reviews by
   */
  fun loadReviews(appSource: AppSource) {
    viewModelScope.launch(dispatcherProvider.io()) {
      try {
        _uiState.value = ReviewListUiState.Loading
        currentAppSource = appSource

        val result = reviewsApi.getAllReviews()

        result.fold(
          onSuccess = { reviews ->
            allReviews = reviews.filter { it.source == appSource }
            Logger.d(TAG, "Loaded ${allReviews.size} reviews for $appSource")
            sortAndUpdate()
          },
          onFailure = { error ->
            Logger.e(TAG, "Failed to load reviews", error)
            _uiState.value = ReviewListUiState.Error(
              error.message ?: "Failed to load reviews"
            )
          }
        )
      } catch (e: Exception) {
        Logger.e(TAG, "Error loading data", e)
        _uiState.value = ReviewListUiState.Error(
          e.message ?: "Unknown error occurred"
        )
      }
    }
  }

  /**
   * Sorts reviews by the specified column.
   * If already sorted by this column, toggles the sort order.
   *
   * @param column The column to sort by
   */
  fun sortBy(column: SortColumn) {
    if (currentSortColumn == column) {
      // Toggle sort order
      currentSortOrder = when (currentSortOrder) {
        SortOrder.ASCENDING -> SortOrder.DESCENDING
        SortOrder.DESCENDING -> SortOrder.ASCENDING
      }
    } else {
      // New column, default to ascending
      currentSortColumn = column
      currentSortOrder = SortOrder.ASCENDING
    }

    sortAndUpdate()
  }

  /**
   * Sorts the reviews based on current sort column and order, then updates the UI state.
   */
  private fun sortAndUpdate() {
    val sortedReviews = when (currentSortColumn) {
      SortColumn.RATING -> {
        if (currentSortOrder == SortOrder.ASCENDING) {
          allReviews.sortedBy { it.rating }
        } else {
          allReviews.sortedByDescending { it.rating }
        }
      }
      SortColumn.COMMENT -> {
        if (currentSortOrder == SortOrder.ASCENDING) {
          allReviews.sortedBy { it.comment.lowercase() }
        } else {
          allReviews.sortedByDescending { it.comment.lowercase() }
        }
      }
      SortColumn.APP_VERSION -> {
        if (currentSortOrder == SortOrder.ASCENDING) {
          allReviews.sortedBy { it.appVersion }
        } else {
          allReviews.sortedByDescending { it.appVersion }
        }
      }
      SortColumn.TIMESTAMP -> {
        if (currentSortOrder == SortOrder.ASCENDING) {
          allReviews.sortedBy { it.timestamp }
        } else {
          allReviews.sortedByDescending { it.timestamp }
        }
      }
      SortColumn.HAS_LOGS -> {
        if (currentSortOrder == SortOrder.ASCENDING) {
          allReviews.sortedBy { it.logFileUrl != null }
        } else {
          allReviews.sortedByDescending { it.logFileUrl != null }
        }
      }
      SortColumn.LOCALE -> {
        if (currentSortOrder == SortOrder.ASCENDING) {
          allReviews.sortedBy { it.userLocale }
        } else {
          allReviews.sortedByDescending { it.userLocale }
        }
      }
    }

    _uiState.value = ReviewListUiState.Success(
      reviews = sortedReviews,
      sortColumn = currentSortColumn,
      sortOrder = currentSortOrder
    )
  }

  companion object {
    private const val TAG = "ReviewListViewModel"
  }
}

/**
 * Represents the different states of the review list UI.
 */
sealed class ReviewListUiState {
  /** Loading data */
  data object Loading : ReviewListUiState()

  /** Data loaded successfully */
  data class Success(
    val reviews: List<Review>,
    val sortColumn: SortColumn,
    val sortOrder: SortOrder
  ) : ReviewListUiState()

  /** Error occurred while loading data */
  data class Error(val message: String) : ReviewListUiState()
}

/**
 * Enum representing sortable columns in the review table.
 */
enum class SortColumn {
  RATING,
  COMMENT,
  APP_VERSION,
  TIMESTAMP,
  HAS_LOGS,
  LOCALE
}

/**
 * Enum representing sort order.
 */
enum class SortOrder {
  ASCENDING,
  DESCENDING
}


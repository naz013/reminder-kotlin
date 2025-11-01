package com.github.nsy.reviewsadmin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.Review
import com.github.naz013.reviews.ReviewsApi
import com.github.nsy.reviewsadmin.cache.ReviewIdCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

/**
 * ViewModel for the dashboard screen.
 *
 * Manages loading and processing of review statistics including:
 * - Total reviews by app source
 * - Average ratings and counts for different time periods per app source
 * - New reviews since last cache
 *
 * @property reviewsApi API for accessing review data
 * @property dispatcherProvider Provides coroutine dispatchers
 * @property reviewIdCache Cache for tracking new reviews
 */
class DashboardViewModel(
  private val reviewsApi: ReviewsApi,
  private val dispatcherProvider: DispatcherProvider,
  private val reviewIdCache: ReviewIdCache
) : ViewModel() {

  private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
  val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

  init {
    loadData()
  }

  /**
   * Loads review data and calculates statistics.
   */
  fun loadData() {
    viewModelScope.launch(dispatcherProvider.io()) {
      try {
        _uiState.value = DashboardUiState.Loading

        val result = reviewsApi.getAllReviews()

        result.fold(
          onSuccess = { reviews ->
            Logger.d(TAG, "Loaded ${reviews.size} reviews")
            val dashboardData = processReviews(reviews)
            _uiState.value = DashboardUiState.Success(dashboardData)
          },
          onFailure = { error ->
            Logger.e(TAG, "Failed to load reviews", error)
            _uiState.value = DashboardUiState.Error(
              error.message ?: "Failed to load reviews"
            )
          }
        )
      } catch (e: Exception) {
        Logger.e(TAG, "Error loading data", e)
        _uiState.value = DashboardUiState.Error(
          e.message ?: "Unknown error occurred"
        )
      }
    }
  }

  /**
   * Processes reviews and calculates statistics.
   *
   * @param reviews List of all reviews
   * @return Processed dashboard data with statistics
   */
  private fun processReviews(reviews: List<Review>): DashboardData {
    val now = LocalDateTime.now()

    // Calculate reviews by source with period statistics for each source
    val reviewsBySource = AppSource.entries.map { source ->
      val sourceReviews = reviews.filter { it.source == source }
      val totalCount = sourceReviews.size

      // Calculate overall average rating for this source
      val totalAverageRating = if (sourceReviews.isNotEmpty()) {
        sourceReviews.map { it.rating.toDouble() }.average()
      } else {
        0.0
      }

      // Calculate new reviews since last cache
      val currentReviewIds = sourceReviews.map { it.id }.toSet()
      val cachedReviewIds = reviewIdCache.getCachedReviewIds(source)
      val newReviewsCount = currentReviewIds.subtract(cachedReviewIds).size

      // Update cache with current review IDs
      reviewIdCache.saveReviewIds(source, currentReviewIds)

      Logger.d(TAG, "Source: $source, Total: $totalCount, Cached: ${cachedReviewIds.size}, New: $newReviewsCount")

      // Calculate period statistics for this specific source
      val periodStatistics = listOf(90, 30, 7).map { days ->
        val cutoffDate = now.minusDays(days.toLong())
        val periodReviews = sourceReviews.filter { it.timestamp.isAfter(cutoffDate) }

        val averageRating = if (periodReviews.isNotEmpty()) {
          periodReviews.map { it.rating.toDouble() }.average()
        } else {
          0.0
        }

        PeriodStatistics(
          days = days,
          averageRating = averageRating,
          count = periodReviews.size
        )
      }

      AppSourceData(
        sourceName = source.name,
        source = source,
        count = totalCount,
        averageRating = totalAverageRating,
        newReviewsCount = newReviewsCount,
        periodStatistics = periodStatistics
      )
    }

    return DashboardData(
      reviewsBySource = reviewsBySource
    )
  }

  companion object {
    private const val TAG = "DashboardViewModel"
  }
}

/**
 * Represents the different states of the dashboard UI.
 */
sealed class DashboardUiState {
  /** Loading data */
  data object Loading : DashboardUiState()

  /** Data loaded successfully */
  data class Success(val data: DashboardData) : DashboardUiState()

  /** Error occurred while loading data */
  data class Error(val message: String) : DashboardUiState()
}

/**
 * Container for all dashboard data.
 *
 * @property reviewsBySource Review counts and statistics grouped by app source
 */
data class DashboardData(
  val reviewsBySource: List<AppSourceData>
)

/**
 * Review count data and period statistics for a specific app source.
 *
 * @property sourceName Display name of the app source
 * @property source The app source enum value
 * @property count Total number of reviews for this source
 * @property averageRating Average rating for all reviews in this source
 * @property newReviewsCount Number of new reviews since last cache
 * @property periodStatistics Statistics for different time periods for this source
 */
data class AppSourceData(
  val sourceName: String,
  val source: AppSource,
  val count: Int,
  val averageRating: Double,
  val newReviewsCount: Int,
  val periodStatistics: List<PeriodStatistics>
)

/**
 * Statistics for a specific time period.
 *
 * @property days Number of days in the period
 * @property averageRating Average rating for reviews in this period
 * @property count Number of reviews in this period
 */
data class PeriodStatistics(
  val days: Int,
  val averageRating: Double,
  val count: Int
)

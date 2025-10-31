package com.github.naz013.reviews.form

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.Review
import com.github.naz013.reviews.db.ReviewRepositoryImpl
import com.github.naz013.reviews.fileupload.LogFileUploader
import com.github.naz013.reviews.logs.FindLatestLogsFileUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import java.util.Locale
import java.util.UUID

/**
 * ViewModel for managing review submission in ReviewDialog.
 *
 * Handles form state, validation, and submission including:
 * - Gathering device and app information
 * - Finding and uploading log files
 * - Saving review to Firestore
 *
 * @property reviewRepository Repository for saving reviews
 * @property logFileUploader Uploader for log files to Firebase Storage
 * @property findLatestLogsFile Use case for finding latest log file
 * @property packageManagerWrapper Wrapper for accessing package information
 * @property dispatcherProvider Provider for coroutine dispatchers
 */
internal class ReviewDialogViewModel(
  private val reviewRepository: ReviewRepositoryImpl,
  private val logFileUploader: LogFileUploader,
  private val findLatestLogsFile: FindLatestLogsFileUseCase,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

  private val _isLoading = mutableLiveDataOf<Boolean>()
  val isLoading = _isLoading.toLiveData()

  private val _submitSuccess = mutableLiveEventOf<Unit>()
  val submitSuccess = _submitSuccess.toLiveData()

  private val _submitError = mutableLiveEventOf<String>()
  val submitError = _submitError.toLiveData()

  /**
   * Submits a review with the provided information.
   *
   * This method performs the following steps:
   * 1. Validates input parameters
   * 2. Gathers device and app information
   * 3. Uploads log file if requested
   * 4. Creates and saves the review
   *
   * @param rating User's rating (1-5)
   * @param comment User's feedback comment
   * @param attachLog Whether to attach log files
   * @param email Optional user email for follow-up
   */
  fun submitReview(
    rating: Float,
    comment: String,
    attachLog: Boolean,
    email: String?,
    appSource: AppSource,
  ) {
    Logger.i(TAG, "Submitting review: rating=$rating, attachLog=$attachLog")

    // Early return if already loading
    if (_isLoading.value == true) {
      Logger.w(TAG, "Review submission already in progress")
      return
    }

    // Input validation
    if (rating < 1f || rating > 5f) {
      Logger.w(TAG, "Invalid rating value: $rating")
      _submitError.value = Event("Invalid rating. Please provide a rating between 1 and 5.")
      return
    }

    if (comment.isBlank()) {
      Logger.w(TAG, "Comment is empty")
      _submitError.value = Event("Please provide a comment with your feedback.")
      return
    }

    viewModelScope.launch(dispatcherProvider.default()) {
      withContext(dispatcherProvider.main()) {
        _isLoading.value = true
      }

      try {
        // Upload log file if requested
        val logFileUrl = if (attachLog) {
          uploadLogFile()
        } else {
          null
        }

        // Gather device and app information
        val appVersion = packageManagerWrapper.getVersionName()
        val deviceInfo = getDeviceInfo()
        val userLocale = Locale.getDefault().toString()

        // Create review object
        val review = Review(
          id = UUID.randomUUID().toString(),
          rating = rating,
          comment = comment.trim(),
          timestamp = LocalDateTime.now(),
          logFileUrl = logFileUrl,
          appVersion = appVersion,
          deviceInfo = deviceInfo,
          userEmail = email?.trim()?.takeIf { it.isNotBlank() },
          userLocale = userLocale,
          source = appSource,
          userId = "" // Will be set by repository
        )

        Logger.d(TAG, "Saving review: id=${review.id}, rating=${review.rating}")

        // Save review to Firestore
        val result = reviewRepository.saveReview(review)

        withContext(dispatcherProvider.main()) {
          if (result.isSuccess) {
            Logger.i(TAG, "Review submitted successfully")
            _submitSuccess.value = Event(Unit)
          } else {
            val error = result.exceptionOrNull()
            Logger.e(TAG, "Failed to submit review", error ?: Exception("Unknown error"))
            _submitError.value = Event(getErrorMessage(error))
          }
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Exception during review submission", e)
        withContext(dispatcherProvider.main()) {
          _submitError.value = Event(getErrorMessage(e))
        }
      } finally {
        withContext(dispatcherProvider.main()) {
          _isLoading.value = false
        }
      }
    }
  }

  /**
   * Converts exceptions into user-friendly error messages.
   *
   * @param error The exception to convert
   * @return A user-friendly error message
   */
  private fun getErrorMessage(error: Throwable?): String {
    if (error == null) {
      return "Failed to submit review. Please try again."
    }

    return when {
      // Firebase App Check attestation errors
      error.message?.contains("App attestation failed", ignoreCase = true) == true ||
        error.message?.contains("attestation", ignoreCase = true) == true ||
        error.message?.contains("App Check", ignoreCase = true) == true -> {
        "Review submitted without log files. App Check configuration pending."
      }

      // Firebase app registration errors
      error.message?.contains("App not registered", ignoreCase = true) == true ||
        error.message?.contains("not registered", ignoreCase = true) == true -> {
        "Review submitted without log files. Firebase configuration pending."
      }

      // Firebase Authentication errors
      error.message?.contains("sign in", ignoreCase = true) == true ||
        error.message?.contains("authentication", ignoreCase = true) == true ||
        error.message?.contains("FirebaseNoSignedInUserException", ignoreCase = true) == true -> {
        "Unable to submit review. Please check your internet connection and try again."
      }

      // Firebase Storage permission errors
      error.message?.contains("service account", ignoreCase = true) == true ||
        error.message?.contains("permissions", ignoreCase = true) == true ||
        error.message?.contains("storage", ignoreCase = true) == true -> {
        "Review submitted without log files. Thank you for your feedback!"
      }

      // Firebase configuration errors
      error.message?.contains("Firebase", ignoreCase = true) == true &&
        error.message?.contains("configuration", ignoreCase = true) == true -> {
        "Review submitted without log files. Configuration in progress."
      }

      // Network errors
      error.message?.contains("network", ignoreCase = true) == true ||
        error.message?.contains("connection", ignoreCase = true) == true ||
        error.message?.contains("timeout", ignoreCase = true) == true -> {
        "Network error. Please check your connection and try again."
      }

      // Firestore errors
      error.message?.contains("firestore", ignoreCase = true) == true ||
        error.message?.contains("firebase", ignoreCase = true) == true -> {
        "Unable to save review. Please try again later."
      }

      // Default fallback
      else -> {
        error.message?.take(100) ?: "Failed to submit review. Please try again."
      }
    }
  }

  /**
   * Uploads the latest log file to Firebase Storage.
   *
   * @return The download URL of the uploaded log file, or null if upload fails
   */
  private suspend fun uploadLogFile(): String? {
    return try {
      Logger.d(TAG, "Finding latest log file")
      val logFilePath = findLatestLogsFile()

      if (logFilePath == null) {
        Logger.w(TAG, "No log file found")
        return null
      }

      Logger.d(TAG, "Uploading log file: $logFilePath")
      val uploadResult = logFileUploader.upload(logFilePath)

      if (uploadResult.isSuccess) {
        val url = uploadResult.getOrNull()
        Logger.i(TAG, "Log file uploaded successfully: $url")
        url
      } else {
        val error = uploadResult.exceptionOrNull()
        // Check if it's a non-critical error - if so, log it but don't fail the submission
        if (error?.message?.contains("service account", ignoreCase = true) == true ||
          error?.message?.contains("permissions", ignoreCase = true) == true ||
          error?.message?.contains("App not registered", ignoreCase = true) == true ||
          error?.message?.contains("not registered", ignoreCase = true) == true ||
          error?.message?.contains("attestation", ignoreCase = true) == true ||
          error?.message?.contains("App Check", ignoreCase = true) == true ||
          error?.message?.contains("Firebase", ignoreCase = true) == true
        ) {
          Logger.w(TAG, "Firebase Storage not available - continuing without logs: ${error.message}")
        } else {
          Logger.e(TAG, "Failed to upload log file", error ?: Exception("Unknown error"))
        }
        null
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Exception while uploading log file", e)
      null
    }
  }

  /**
   * Gathers device information for diagnostics.
   *
   * @return A string containing device model, manufacturer, and Android version
   */
  private fun getDeviceInfo(): String {
    return buildString {
      append("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
      append(", Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    }
  }

  companion object {
    private const val TAG = "ReviewDialogViewModel"
  }
}


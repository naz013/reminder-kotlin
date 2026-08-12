package com.github.naz013.reviews

import org.threeten.bp.LocalDateTime

/**
 * Represents the source of the application version.
 */
enum class AppSource {
  FREE,
  PRO
}

/**
 * Represents a user review.
 *
 * @property id Unique identifier for the review
 * @property rating User rating (1-5 stars), defaults to 5
 * @property comment User's review comment
 * @property timestamp When the review was submitted
 * @property logFileUrl Optional URL to log file
 * @property appVersion Version of the application
 * @property deviceInfo Information about the user's device
 * @property userEmail Optional user email address
 * @property userLocale User's locale setting
 * @property userId User identifier
 * @property source Application source (Free or Pro version)
 * @property processed Flag indicating if the review has been processed, defaults to false
 */
data class Review(
  val id: String,
  val rating: Float = 5f,
  val comment: String,
  val timestamp: LocalDateTime,
  val logFileUrl: String?,
  val appVersion: String,
  val deviceInfo: String,
  val userEmail: String?,
  val userLocale: String,
  val userId: String = "",
  val source: AppSource = AppSource.FREE,
  val processed: Boolean = false
)

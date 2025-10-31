package com.github.naz013.reviews.db

import com.github.naz013.reviews.Review

/**
 * Converts a Review domain object to a ReviewEntity for database storage.
 *
 * @return ReviewEntity representation of this Review
 */
internal fun Review.toReviewEntity(): ReviewEntity {
  return ReviewEntity(
    id = id,
    rating = rating,
    comment = comment,
    timestamp = timestamp.toEpochMillis(),
    logFileUrl = logFileUrl,
    appVersion = appVersion,
    deviceInfo = deviceInfo,
    userEmail = userEmail,
    userLocale = userLocale,
    userId = userId
  )
}

/**
 * Converts a ReviewEntity from database to a Review domain object.
 *
 * @return Review domain object representation of this ReviewEntity
 */
internal fun ReviewEntity.toReview(): Review {
  return Review(
    id = id,
    rating = rating,
    comment = comment,
    timestamp = timestamp.toLocalDateTime(),
    logFileUrl = logFileUrl,
    appVersion = appVersion,
    deviceInfo = deviceInfo,
    userEmail = userEmail,
    userLocale = userLocale,
    userId = userId
  )
}

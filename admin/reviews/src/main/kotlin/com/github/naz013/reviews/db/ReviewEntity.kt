package com.github.naz013.reviews.db

import com.google.gson.annotations.SerializedName

/**
 * Entity class representing a review in Firestore.
 * Default values are provided for Firestore deserialization.
 */
internal data class ReviewEntity(
  @SerializedName("id")
  val id: String = "",
  @SerializedName("rating")
  val rating: Float = 0f,
  @SerializedName("comment")
  val comment: String = "",
  @SerializedName("timestamp")
  val timestamp: Long = 0L,
  @SerializedName("logFileUrl")
  val logFileUrl: String? = null,
  @SerializedName("appVersion")
  val appVersion: String = "",
  @SerializedName("deviceInfo")
  val deviceInfo: String = "",
  @SerializedName("userEmail")
  val userEmail: String? = null,
  @SerializedName("userLocale")
  val userLocale: String = "",
  @SerializedName("userId")
  val userId: String = "",
  @SerializedName("appSource")
  val appSource: String = "",
  @SerializedName("processed")
  val processed: Boolean = false
)

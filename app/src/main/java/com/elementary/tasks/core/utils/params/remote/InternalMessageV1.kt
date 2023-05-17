package com.elementary.tasks.core.utils.params.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class InternalMessageV1(
  @SerializedName("message")
  val message: String,
  @SerializedName("startAt")
  val startAt: String,
  @SerializedName("endAt")
  val endAt: String,
  @SerializedName("localized")
  val localized: List<LocalizedMessage> = emptyList()
)

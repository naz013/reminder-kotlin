package com.elementary.tasks.core.utils.params.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpdateMessageV2(
  @SerializedName("versionCode")
  val versionCode: Long,
  @SerializedName("versionName")
  val versionName: String,
  @SerializedName("createdAt")
  val createdAt: String
)

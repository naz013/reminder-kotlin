package com.elementary.tasks.core.utils.params.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LocalizedMessage(
  @SerializedName("text")
  val text: String,
  @SerializedName("lang")
  val lang: String
)

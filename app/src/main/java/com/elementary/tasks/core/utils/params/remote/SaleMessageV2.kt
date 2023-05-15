package com.elementary.tasks.core.utils.params.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SaleMessageV2(
  @SerializedName("salePercentage")
  val salePercentage: String,
  @SerializedName("startAt")
  val startAt: String,
  @SerializedName("endAt")
  val endAt: String
)

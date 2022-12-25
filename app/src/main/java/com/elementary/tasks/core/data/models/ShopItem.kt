package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Keep
@Parcelize
data class ShopItem(
  @SerializedName("summary")
  var summary: String = "",
  @SerializedName("isDeleted")
  var isDeleted: Boolean = false,
  @SerializedName("checked")
  var isChecked: Boolean = false,
  @SerializedName("uuId")
  var uuId: String = UUID.randomUUID().toString(),
  @SerializedName("createTime")
  var createTime: String = DateTimeManager.gmtDateTime
) : Parcelable
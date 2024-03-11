package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.elementary.tasks.reminder.build.bi.BiType
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class BuilderSchemeItem(
  @SerializedName("type")
  val type: BiType,
  @SerializedName("position")
  val position: Int
) : Parcelable

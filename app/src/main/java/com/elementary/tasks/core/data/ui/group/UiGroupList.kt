package com.elementary.tasks.core.data.ui.group

import androidx.annotation.ColorInt
import com.google.gson.annotations.SerializedName

data class UiGroupList(
  @SerializedName("id")
  val id: String,
  @SerializedName("title")
  val title: String,
  @SerializedName("color")
  @ColorInt
  val color: Int,
  @SerializedName("colorPosition")
  val colorPosition: Int,
  @SerializedName("contrastColor")
  val contrastColor: Int
)

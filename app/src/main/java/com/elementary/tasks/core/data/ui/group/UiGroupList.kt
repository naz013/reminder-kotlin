package com.elementary.tasks.core.data.ui.group

import androidx.annotation.ColorInt

data class UiGroupList(
  val id: String,
  val title: String,
  @ColorInt
  val color: Int,
  val colorPosition: Int
)

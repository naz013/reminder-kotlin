package com.elementary.tasks.core.data.ui

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class UiIcon(
  @DrawableRes
  val value: Int,
  @ColorInt
  val color: Int
)

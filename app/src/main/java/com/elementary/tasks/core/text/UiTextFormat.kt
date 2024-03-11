package com.elementary.tasks.core.text

import android.graphics.Typeface
import androidx.annotation.ColorInt

data class UiTextFormat(
  val fontSize: Float,
  @ColorInt
  val textColor: Int? = null,
  val font: Typeface? = null,
  val textStyle: UiTextStyle = UiTextStyle.NORMAL,
  val textDecoration: UiTextDecoration = UiTextDecoration.NONE
)

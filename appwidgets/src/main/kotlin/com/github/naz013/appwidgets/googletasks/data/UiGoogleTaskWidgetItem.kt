package com.github.naz013.appwidgets.googletasks.data

import androidx.compose.ui.graphics.Color

internal data class UiGoogleTaskWidgetItem(
  val taskId: String,
  val title: String,
  val note: String?,
  val dateText: String?,
  val iconRes: Int,
  val iconTintColor: Color
)

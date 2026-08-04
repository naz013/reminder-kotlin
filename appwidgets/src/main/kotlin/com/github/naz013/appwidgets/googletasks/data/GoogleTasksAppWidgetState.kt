package com.github.naz013.appwidgets.googletasks.data

import androidx.compose.ui.graphics.Color

internal data class GoogleTasksAppWidgetState(
  val widgetId: Int,
  val headerBackgroundColor: Int,
  val headerContrastColor: Color,
  val itemBackgroundColor: Int,
  val itemContrastColor: Color,
  val items: List<UiGoogleTaskWidgetItem>
)

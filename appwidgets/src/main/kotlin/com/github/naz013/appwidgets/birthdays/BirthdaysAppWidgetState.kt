package com.github.naz013.appwidgets.birthdays

import androidx.compose.ui.graphics.Color

internal data class BirthdaysAppWidgetState(
  val widgetId: Int,
  val headerBackgroundColor: Int,
  val headerContrastColor: Color,
  val itemBackgroundColor: Int,
  val itemContrastColor: Color,
  val items: List<UiBirthdayWidgetList>
)

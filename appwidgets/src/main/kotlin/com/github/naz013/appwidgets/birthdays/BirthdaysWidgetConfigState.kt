package com.github.naz013.appwidgets.birthdays

import androidx.compose.ui.graphics.Color

internal data class BirthdaysWidgetConfigState(
  val headerBackgroundIndex: Int = 0,
  val itemBackgroundIndex: Int = 0,
  val hapticFeedbackEnabled: Boolean = true,
  val palette: List<Color> = emptyList(),
  val headerColor: Color = Color.Unspecified,
  val headerContentColor: Color = Color.Unspecified,
  val itemColor: Color = Color.Unspecified,
  val itemContentColor: Color = Color.Unspecified,
)

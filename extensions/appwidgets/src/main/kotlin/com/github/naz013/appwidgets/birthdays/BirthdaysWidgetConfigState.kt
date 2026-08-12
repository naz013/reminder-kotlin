package com.github.naz013.appwidgets.birthdays

import androidx.compose.ui.graphics.Color

internal data class BirthdaysWidgetConfigState(
  val backgroundColorIndex: Int = 0,
  val hapticFeedbackEnabled: Boolean = true,
  val palette: List<Color> = emptyList(),
  val backgroundColor: Color = Color.Unspecified,
  val foregroundColor: Color = Color.Unspecified,
)

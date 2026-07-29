package com.github.naz013.appwidgets.calendar

import androidx.compose.ui.graphics.Color

internal data class CalendarWidgetConfigState(
  val headerBackgroundIndex: Int = 0,
  val backgroundIndex: Int = 0,
  val hapticFeedbackEnabled: Boolean = true,
  val palette: List<Color> = emptyList(),
  val headerColor: Color = Color.Unspecified,
  val headerContentColor: Color = Color.Unspecified,
  val backgroundColor: Color = Color.Unspecified,
)

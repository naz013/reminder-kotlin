package com.github.naz013.appwidgets.nextreminder

import androidx.compose.ui.graphics.Color

internal data class NextReminderWidgetConfigState(
  val backgroundIndex: Int = 0,
  val hapticFeedbackEnabled: Boolean = true,
  val palette: List<Color> = emptyList(),
  val backgroundColor: Color = Color.Unspecified,
  val contentColor: Color = Color.Unspecified,
)

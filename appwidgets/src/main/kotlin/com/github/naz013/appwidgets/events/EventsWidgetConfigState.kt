package com.github.naz013.appwidgets.events

import androidx.compose.ui.graphics.Color

internal data class EventsWidgetConfigState(
  val backgroundIndex: Int = 0,
  val textSize: Int = 14,
  val isTextSizeDialogVisible: Boolean = false,
  val hapticFeedbackEnabled: Boolean = true,
  val palette: List<Color> = emptyList(),
  val backgroundColor: Color = Color.Unspecified,
  val foregroundColor: Color = Color.Unspecified,
)

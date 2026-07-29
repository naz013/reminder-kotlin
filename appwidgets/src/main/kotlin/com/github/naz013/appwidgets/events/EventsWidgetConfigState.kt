package com.github.naz013.appwidgets.events

import androidx.compose.ui.graphics.Color

internal data class EventsWidgetConfigState(
  val headerBackgroundIndex: Int = 0,
  val itemBackgroundIndex: Int = 0,
  val textSize: Int = 14,
  val isTextSizeDialogVisible: Boolean = false,
  val hapticFeedbackEnabled: Boolean = true,
  val palette: List<Color> = emptyList(),
  val headerColor: Color = Color.Unspecified,
  val headerContentColor: Color = Color.Unspecified,
  val itemColor: Color = Color.Unspecified,
  val itemContentColor: Color = Color.Unspecified,
)

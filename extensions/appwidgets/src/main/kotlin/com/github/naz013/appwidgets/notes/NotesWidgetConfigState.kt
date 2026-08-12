package com.github.naz013.appwidgets.notes

import androidx.compose.ui.graphics.Color

internal data class NotesWidgetConfigState(
  val backgroundIndex: Int = 0,
  val hapticFeedbackEnabled: Boolean = true,
  val palette: List<Color> = emptyList(),
  val backgroundColor: Color = Color.Unspecified,
  val foregroundColor: Color = Color.Unspecified,
)

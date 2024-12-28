package com.github.naz013.appwidgets.events.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

internal data class EventsAppWidgetState(
  val widgetId: Int,
  val headerText: String,
  val headerBackgroundColor: Int,
  val headerContrastColor: Color,
  val itemBackgroundColor: Int,
  val itemContrastColor: Color,
  val itemTextSize: TextUnit,
  val items: List<DateSorted>
)

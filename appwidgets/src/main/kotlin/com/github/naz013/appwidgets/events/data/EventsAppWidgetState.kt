package com.github.naz013.appwidgets.events.data

import androidx.compose.ui.unit.TextUnit

internal data class EventsAppWidgetState(
  val widgetId: Int,
  val headerText: String,
  val backgroundColor: Int,
  val itemTextSize: TextUnit,
  val items: List<DateSorted>
)

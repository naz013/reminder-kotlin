package com.github.naz013.appwidgets.todo

import androidx.compose.ui.graphics.Color

internal data class TodoReminderItem(
  val id: String,
  val summary: String,
  val itemsCount: Int
)

internal data class TodoWidgetConfigState(
  val reminders: List<TodoReminderItem> = emptyList(),
  val selectedReminderId: String? = null,
  val backgroundIndex: Int = 0,
  val hapticFeedbackEnabled: Boolean = true,
  val palette: List<Color> = emptyList(),
  val backgroundColor: Color = Color.Unspecified,
  val foregroundColor: Color = Color.Unspecified
)

internal sealed interface TodoWidgetConfigEvent {
  data object ReminderNotSelected : TodoWidgetConfigEvent
  data object Saved : TodoWidgetConfigEvent
}

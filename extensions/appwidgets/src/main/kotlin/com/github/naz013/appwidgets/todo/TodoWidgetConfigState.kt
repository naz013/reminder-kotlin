package com.github.naz013.appwidgets.todo

internal data class TodoReminderItem(
  val id: String,
  val summary: String,
  val itemsCount: Int
)

internal data class TodoWidgetConfigState(
  val reminders: List<TodoReminderItem> = emptyList(),
  val selectedReminderId: String? = null
)

internal sealed interface TodoWidgetConfigEvent {
  data object ReminderNotSelected : TodoWidgetConfigEvent
  data object Saved : TodoWidgetConfigEvent
}

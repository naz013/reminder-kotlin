package com.github.naz013.appwidgets.habit

internal data class UiHabitWidgetItem(
  val id: String,
  val name: String,
  val progressLabel: String,
  val isDone: Boolean
)

internal data class HabitAppWidgetState(
  val widgetId: Int,
  val backgroundColor: Int = 0,
  val isUnlocked: Boolean = true,
  val items: List<UiHabitWidgetItem> = emptyList()
)

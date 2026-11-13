package com.github.naz013.appwidgets.routine

internal data class UiRoutineWidgetItem(
  val id: String,
  val name: String,
  val iconRes: Int?,
  val backgroundColor: Int,
  val foregroundColor: Int,
  val stepCountLabel: String,
  val durationLabel: String,
  val statusLabel: String,
  val isRunning: Boolean
)

internal data class RoutineAppWidgetState(
  val widgetId: Int,
  val isUnlocked: Boolean = true,
  val item: UiRoutineWidgetItem? = null
)

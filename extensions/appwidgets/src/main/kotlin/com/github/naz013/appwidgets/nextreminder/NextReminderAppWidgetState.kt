package com.github.naz013.appwidgets.nextreminder

internal data class NextReminderAppWidgetState(
  val widgetId: Int,
  val backgroundColor: Int,
  val uuId: String?,
  val title: String?,
  val dateTime: String?
)

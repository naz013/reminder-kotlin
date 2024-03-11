package com.elementary.tasks.reminder.preview.data

data class UiCalendarEventList(
  val title: String,
  val description: String,
  val calendarName: String?,
  val dateStartFormatted: String?,
  val dateEndFormatted: String?,
  val id: Long,
  val localId: String
)

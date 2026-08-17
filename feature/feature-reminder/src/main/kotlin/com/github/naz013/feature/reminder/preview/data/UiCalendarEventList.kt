package com.github.naz013.feature.reminder.preview.data

internal data class UiCalendarEventList(
  val title: String,
  val description: String,
  val calendarName: String?,
  val dateStartFormatted: String?,
  val dateEndFormatted: String?,
  val id: Long,
  val localId: String,
)

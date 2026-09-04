package com.github.naz013.feature.calendar.preview

internal data class GoogleCalendarEventPreviewState(
  val isLoading: Boolean = true,
  val title: String = "",
  val calendarName: String = "",
  val description: String = "",
  val dateTimeFormatted: String = "",
  val allDay: Boolean = false,
  val showDeleteOptions: Boolean = false,
)

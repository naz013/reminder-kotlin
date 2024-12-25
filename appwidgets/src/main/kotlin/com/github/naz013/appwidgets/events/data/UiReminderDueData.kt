package com.github.naz013.appwidgets.events.data

internal data class UiReminderDueData(
  val dateTime: String?,
  val remaining: String?,
  val millis: Long = 0
)

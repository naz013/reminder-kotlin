package com.elementary.tasks.core.data.ui.reminder

data class UiReminderDueData(
  val dateTime: String?,
  val repeat: String,
  val before: String?,
  val remaining: String?,
  val millis: Long = 0
)

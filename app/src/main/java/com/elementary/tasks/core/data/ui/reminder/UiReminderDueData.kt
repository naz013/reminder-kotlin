package com.elementary.tasks.core.data.ui.reminder

import org.threeten.bp.LocalDateTime

data class UiReminderDueData(
  val dateTime: String?,
  val repeat: String,
  val before: String?,
  val remaining: String?,
  val millis: Long = 0,
  val localDateTime: LocalDateTime? = null,
  val recurRule: String? = null
)

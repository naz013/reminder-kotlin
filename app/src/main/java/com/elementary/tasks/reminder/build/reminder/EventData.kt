package com.elementary.tasks.reminder.build.reminder

import org.threeten.bp.LocalDateTime

data class EventData(
  val startDateTime: LocalDateTime,
  val recurObject: String
)

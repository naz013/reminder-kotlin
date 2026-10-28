package com.github.naz013.ui.reminder

import org.threeten.bp.LocalDateTime

data class UiReminderDueData(
  val dateTime: String?,
  val repeat: String,
  val before: String?,
  val remaining: String?,
  val millis: Long = 0,
  val localDateTime: LocalDateTime? = null,
  val recurRule: String? = null,
  val formattedTime: String?,
  val formattedDateTime: String?,
  /** Time in the reminder's own zone, e.g. "09:00 Berlin"; null unless it is a fixed zone other than the device's. */
  val zoneLabel: String? = null,
)

package com.elementary.tasks.core.app_widgets.events

import org.threeten.bp.LocalDateTime

data class CalendarItem(
  val type: Type? = null,
  val summary: String? = null,
  val number: String? = null,
  val timeFormatted: String? = null,
  val dateFormatted: String? = null,
  val id: String? = null,
  val dateTime: LocalDateTime? = null,
  val viewType: Int = 0,
  val item: Any? = null,
) {
  enum class Type {
    BIRTHDAY, REMINDER
  }
}

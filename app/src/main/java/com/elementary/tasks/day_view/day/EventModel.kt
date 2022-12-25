package com.elementary.tasks.day_view.day

import com.elementary.tasks.core.data.ui.UiBirthdayList
import com.elementary.tasks.core.data.ui.UiReminderListActive

data class EventModel(
  val viewType: Int,
  var model: Any,
  val day: Int,
  val month: Int,
  val year: Int,
  val dt: Long,
  val color: Int
) {
  fun getMillis(): Long {
    return when (val o = model) {
      is UiReminderListActive -> o.due.millis
      is UiBirthdayList -> o.nextBirthdayDate
      else -> 0
    }
  }

  companion object {
    const val REMINDER = 0
    const val BIRTHDAY = 2
  }
}

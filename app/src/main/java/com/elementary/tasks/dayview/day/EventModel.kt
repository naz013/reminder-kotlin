package com.elementary.tasks.dayview.day

import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.data.ui.UiReminderListActive

data class EventModel(
  val viewType: Int,
  var model: Any,
  val day: Int,
  val monthValue: Int,
  val year: Int,
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

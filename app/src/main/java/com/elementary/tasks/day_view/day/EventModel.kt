package com.elementary.tasks.day_view.day

import com.elementary.tasks.birthdays.list.BirthdayListItem
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeUtil

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
      is Reminder -> TimeUtil.getDateTimeFromGmt(o.eventTime)
      is BirthdayListItem -> o.nextBirthdayDate
      else -> 0
    }
  }

  companion object {
    const val REMINDER = 0
    const val BIRTHDAY = 2
  }
}

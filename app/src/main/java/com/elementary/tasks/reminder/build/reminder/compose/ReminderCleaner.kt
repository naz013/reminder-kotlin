package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType

class ReminderCleaner {

  operator fun invoke(reminder: Reminder) {
    val type = UiReminderType(reminder.type)
    when {
      type.isByDate() -> {
        Traces.d(TAG, "invoke: clean up for ${reminder.type} as BY_DATE")
        reminder.weekdays = listOf()
        reminder.dayOfMonth = 0
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.recurDataObject = null
      }

      type.isTimer() -> {
        Traces.d(TAG, "invoke: clean up for ${reminder.type} as BY_TIME")
        reminder.weekdays = listOf()
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.recurDataObject = null
      }

      type.isByWeekday() -> {
        Traces.d(TAG, "invoke: clean up for ${reminder.type} as BY_WEEK")
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        reminder.recurDataObject = null
      }

      type.isMonthly() -> {
        Traces.d(TAG, "invoke: clean up for ${reminder.type} as BY_MONTH")
        reminder.weekdays = listOf()
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.recurDataObject = null
      }

      type.isYearly() -> {
        Traces.d(TAG, "invoke: clean up for ${reminder.type} as BY_DAY_OF_YEAR")
        reminder.weekdays = listOf()
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        reminder.recurDataObject = null
      }

      type.isGpsType() -> {
        Traces.d(TAG, "invoke: clean up for ${reminder.type} as BY_GPS")
        reminder.exportToCalendar = false
        reminder.exportToTasks = false
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        reminder.recurDataObject = null
      }

      type.isRecur() -> {
        Traces.d(TAG, "invoke: clean up for ${reminder.type} as ICAL")
        reminder.weekdays = listOf()
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
      }

      type.isSubTasks() -> {
        Traces.d(TAG, "invoke: clean up for ${reminder.type} as SUB_TASKS")
        reminder.target = ""
      }

      else -> {
        Traces.d(TAG, "invoke: nothing to clean up for ${reminder.type}")
      }
    }
  }

  companion object {
    private const val TAG = "ReminderCleaner"
  }
}

package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

class ReminderDateTimeCleaner {

  operator fun invoke(reminder: Reminder) {
    val type = UiReminderType(reminder.type)
    when {
      type.isByDate() -> {
        Logger.i(TAG, "Clean up for ${reminder.type} as BY_DATE")
        reminder.weekdays = listOf()
        reminder.dayOfMonth = 0
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.recurDataObject = null
      }

      type.isTimer() -> {
        Logger.i(TAG, "Clean up for ${reminder.type} as BY_TIME")
        reminder.weekdays = listOf()
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.recurDataObject = null
      }

      type.isByWeekday() -> {
        Logger.i(TAG, "Clean up for ${reminder.type} as BY_WEEK")
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        reminder.recurDataObject = null
      }

      type.isMonthly() -> {
        Logger.i(TAG, "Clean up for ${reminder.type} as BY_MONTH")
        reminder.weekdays = listOf()
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.recurDataObject = null
      }

      type.isYearly() -> {
        Logger.i(TAG, "Clean up for ${reminder.type} as BY_DAY_OF_YEAR")
        reminder.weekdays = listOf()
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        reminder.recurDataObject = null
      }

      type.isGpsType() -> {
        Logger.i(TAG, "Clean up for ${reminder.type} as BY_GPS")
        reminder.exportToCalendar = false
        reminder.exportToTasks = false
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        reminder.recurDataObject = null
      }

      type.isRecur() -> {
        Logger.i(TAG, "Clean up for ${reminder.type} as ICAL")
        reminder.weekdays = listOf()
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
      }

      type.isSubTasks() -> {
        Logger.i(TAG, "Clean up for ${reminder.type} as SUB_TASKS")
        reminder.target = ""
      }

      else -> {
        Logger.i(TAG, "Nothing to clean up for ${reminder.type}")
      }
    }
  }

  companion object {
    private const val TAG = "ReminderCleaner"
  }
}

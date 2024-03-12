package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import timber.log.Timber

class ReminderCleaner {

  operator fun invoke(reminder: Reminder) {
    val type = UiReminderType(reminder.type)
    when {
      type.isByDate() -> {
        Timber.d("invoke: clean up for ${reminder.type} as BY_DATE")
        reminder.weekdays = listOf()
        reminder.dayOfMonth = 0
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.recurDataObject = null
      }

      type.isTimer() -> {
        Timber.d("invoke: clean up for ${reminder.type} as BY_TIME")
        reminder.weekdays = listOf()
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.recurDataObject = null
      }

      type.isByWeekday() -> {
        Timber.d("invoke: clean up for ${reminder.type} as BY_WEEK")
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        reminder.recurDataObject = null
      }

      type.isMonthly() -> {
        Timber.d("invoke: clean up for ${reminder.type} as BY_MONTH")
        reminder.weekdays = listOf()
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.recurDataObject = null
      }

      type.isYearly() -> {
        Timber.d("invoke: clean up for ${reminder.type} as BY_DAY_OF_YEAR")
        reminder.weekdays = listOf()
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        reminder.recurDataObject = null
      }

      type.isGpsType() -> {
        Timber.d("invoke: clean up for ${reminder.type} as BY_GPS")
        reminder.exportToCalendar = false
        reminder.exportToTasks = false
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
        reminder.recurDataObject = null
      }

      type.isRecur() -> {
        Timber.d("invoke: clean up for ${reminder.type} as ICAL")
        reminder.weekdays = listOf()
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0
      }

      type.isSubTasks() -> {
        Timber.d("invoke: clean up for ${reminder.type} as SUB_TASKS")
        reminder.target = ""
      }

      else -> {
        Timber.d("invoke: nothing to clean up for ${reminder.type}")
      }
    }
  }
}

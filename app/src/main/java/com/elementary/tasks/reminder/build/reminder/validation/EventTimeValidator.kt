package com.elementary.tasks.reminder.build.reminder.validation

import com.github.naz013.domain.Reminder

class EventTimeValidator {

  operator fun invoke(reminder: Reminder): Boolean {
    val type = reminder.readType()
    return if (type.isICalendar() || type.isByDayOfYear() || type.isCountdown() ||
      type.isByDayOfWeek() || type.isByDayOfMonth()
    ) {
      reminder.eventTime.isNotEmpty()
    } else if (type.isDateTime()) {
      if (type.hasSubTasks()) {
        if (reminder.hasReminder) {
          reminder.eventTime.isNotEmpty()
        } else {
          true
        }
      } else {
        reminder.eventTime.isNotEmpty()
      }
    } else if (type.isGpsType()) {
      if (reminder.hasReminder) {
        reminder.eventTime.isNotEmpty()
      } else {
        true
      }
    } else {
      true
    }
  }
}

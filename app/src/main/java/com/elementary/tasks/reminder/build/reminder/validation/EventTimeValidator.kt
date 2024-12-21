package com.elementary.tasks.reminder.build.reminder.validation

import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType

class EventTimeValidator {

  operator fun invoke(reminder: Reminder): Boolean {
    val type = UiReminderType(reminder.type)
    return if (type.isBase(UiReminderType.Base.RECUR) || type.isBase(UiReminderType.Base.YEARLY) ||
      type.isBase(UiReminderType.Base.TIMER) || type.isBase(UiReminderType.Base.WEEKDAY) ||
      type.isBase(UiReminderType.Base.MONTHLY)
    ) {
      reminder.eventTime.isNotEmpty()
    } else if (type.isBase(UiReminderType.Base.DATE)) {
      if (type.isSubTasks()) {
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

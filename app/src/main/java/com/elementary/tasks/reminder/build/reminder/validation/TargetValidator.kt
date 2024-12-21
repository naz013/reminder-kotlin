package com.elementary.tasks.reminder.build.reminder.validation

import com.github.naz013.domain.Reminder

class TargetValidator {

  operator fun invoke(reminder: Reminder): Boolean {
    if (!shouldCheckTarget(reminder)) {
      return true
    }
    return reminder.target.isNotEmpty()
  }

  private fun shouldCheckTarget(reminder: Reminder): Boolean {
    return reminder.type % 10 == Reminder.Action.APP ||
      reminder.type % 10 == Reminder.Action.CALL ||
      reminder.type % 10 == Reminder.Action.SMS ||
      reminder.type % 10 == Reminder.Action.EMAIL ||
      reminder.type % 10 == Reminder.Action.LINK
  }
}

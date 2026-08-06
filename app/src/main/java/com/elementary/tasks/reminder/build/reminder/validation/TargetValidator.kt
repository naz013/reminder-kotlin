package com.elementary.tasks.reminder.build.reminder.validation

import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2

class TargetValidator {
  operator fun invoke(reminder: ReminderV2): Boolean =
    when (val action = reminder.action) {
      is ReminderAction.App -> action.target.isNotEmpty()
      is ReminderAction.Call -> action.target.isNotEmpty()
      is ReminderAction.Sms -> action.target.isNotEmpty()
      is ReminderAction.Email -> action.target.isNotEmpty()
      is ReminderAction.Link -> action.target.isNotEmpty()
      ReminderAction.Shopping, ReminderAction.None -> true
    }
}

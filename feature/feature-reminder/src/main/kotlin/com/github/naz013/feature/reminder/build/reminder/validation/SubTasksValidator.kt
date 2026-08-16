package com.github.naz013.feature.reminder.build.reminder.validation

import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2

class SubTasksValidator {
  operator fun invoke(reminder: ReminderV2): Boolean =
    if (reminder.action is ReminderAction.Shopping) {
      reminder.shoppingItems.isNotEmpty()
    } else {
      true
    }
}

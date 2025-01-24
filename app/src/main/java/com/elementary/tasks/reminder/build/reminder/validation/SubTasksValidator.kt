package com.elementary.tasks.reminder.build.reminder.validation

import com.github.naz013.domain.Reminder

class SubTasksValidator {

  operator fun invoke(reminder: Reminder): Boolean {
    return if (reminder.readType().hasSubTasks()) {
      reminder.shoppings.isNotEmpty()
    } else {
      true
    }
  }
}

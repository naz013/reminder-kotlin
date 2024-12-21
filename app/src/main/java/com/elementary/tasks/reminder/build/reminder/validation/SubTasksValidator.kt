package com.elementary.tasks.reminder.build.reminder.validation

import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType

class SubTasksValidator {

  operator fun invoke(reminder: Reminder): Boolean {
    val type = UiReminderType(reminder.type)
    return if (type.isSubTasks()) {
      reminder.shoppings.isNotEmpty()
    } else {
      true
    }
  }
}

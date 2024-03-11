package com.elementary.tasks.reminder.build.reminder.validation

import com.elementary.tasks.core.data.models.Reminder

class ReminderValidator(
  private val targetValidator: TargetValidator,
  private val subTasksValidator: SubTasksValidator,
  private val eventTimeValidator: EventTimeValidator
) {

  operator fun invoke(reminder: Reminder): ValidationResult {
    if (!targetValidator(reminder)) {
      return ValidationResult.Failed(ValidationError.TARGET)
    }
    if (!subTasksValidator(reminder)) {
      return ValidationResult.Failed(ValidationError.SUB_TASKS)
    }
    if (!eventTimeValidator(reminder)) {
      return ValidationResult.Failed(ValidationError.EVENT_TIME)
    }

    return ValidationResult.Success
  }

  sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Failed(
      val error: ValidationError
    ) : ValidationResult()
  }

  enum class ValidationError {
    TARGET,
    SUB_TASKS,
    EVENT_TIME
  }
}

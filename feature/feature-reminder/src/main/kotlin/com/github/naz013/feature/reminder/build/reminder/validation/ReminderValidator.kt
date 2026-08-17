package com.github.naz013.feature.reminder.build.reminder.validation

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

class ReminderValidator(
  private val targetValidator: TargetValidator,
  private val subTasksValidator: SubTasksValidator,
  private val eventTimeValidator: EventTimeValidator,
) {
  operator fun invoke(reminder: ReminderV2): ValidationResult {
    if (!targetValidator(reminder)) {
      Logger.e(TAG, "Reminder target is not valid")
      return ValidationResult.Failed(ValidationError.TARGET)
    }
    if (!subTasksValidator(reminder)) {
      Logger.e(TAG, "Reminder has invalid sub tasks")
      return ValidationResult.Failed(ValidationError.SUB_TASKS)
    }
    if (!eventTimeValidator(reminder)) {
      Logger.e(TAG, "Reminder event time is not valid")
      return ValidationResult.Failed(ValidationError.EVENT_TIME)
    }

    Logger.i(TAG, "Reminder is valid")
    return ValidationResult.Success
  }

  sealed class ValidationResult {
    data object Success : ValidationResult()

    data class Failed(
      val error: ValidationError,
    ) : ValidationResult()
  }

  enum class ValidationError {
    TARGET,
    SUB_TASKS,
    EVENT_TIME,
  }

  companion object {
    private const val TAG = "ReminderValidator"
  }
}

package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2

/**
 * Snoozes a reminder for a specified time in minutes.
 *
 * Implemented in `app` (`SnoozeReminderUseCaseImpl`) rather than here, because it depends on
 * `feature-workflow`'s `WorkflowTriggerRunner` - a concrete class, not an interface - which
 * `logic-reminder` can't depend on without creating a `feature-workflow` <-> `logic-reminder` cycle
 * (feature-workflow already depends on logic-reminder).
 */
interface SnoozeReminderUseCase {
  suspend operator fun invoke(
    reminder: ReminderV2,
    timeInMinutes: Int,
  ): ReminderV2
}

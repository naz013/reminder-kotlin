package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2

/**
 * Snoozes a reminder for a specified time in minutes.
 */
interface SnoozeReminderUseCase {
  suspend operator fun invoke(
    reminder: ReminderV2,
    timeInMinutes: Int,
  ): ReminderV2
}

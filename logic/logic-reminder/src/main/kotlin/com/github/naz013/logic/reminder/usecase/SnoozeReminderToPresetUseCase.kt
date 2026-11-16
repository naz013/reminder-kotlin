package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.snooze.SnoozePresetType

/**
 * Snoozes a reminder until the time a [SnoozePresetType] resolves to.
 */
interface SnoozeReminderToPresetUseCase {
  suspend operator fun invoke(
    reminder: ReminderV2,
    preset: SnoozePresetType,
  ): ReminderV2
}

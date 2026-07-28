package com.github.naz013.repository.migration

import com.github.naz013.domain.reminder.migration.toReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderV2Repository

/**
 * One-time migration step: populates the ReminderV2 table from existing V1 Reminder rows.
 * Intended to be invoked once at app startup (not from [androidx.room.migration.Migration.migrate],
 * which must stay fast/synchronous) behind a one-shot flag owned by the caller.
 */
class ReminderV2BackfillUseCase internal constructor(
  private val reminderRepository: ReminderRepository,
  private val reminderV2Repository: ReminderV2Repository
) {

  suspend operator fun invoke() {
    val existingIds = reminderV2Repository.getAllIds().toSet()
    val reminders = reminderRepository.getAll()
    Logger.d(TAG, "Backfilling ${reminders.size} reminders into ReminderV2")
    reminders
      .filterNot { existingIds.contains(it.uuId) }
      .forEach { reminder ->
        runCatching {
          reminderV2Repository.save(reminder.toReminderV2())
        }.onFailure {
          Logger.e(TAG, "Failed to backfill reminder ${reminder.uuId}", it)
        }
      }
  }

  companion object {
    private const val TAG = "ReminderV2Backfill"
  }
}

package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger

/**
 * Opts an offline-only reminder back into cloud sync - the one-way counterpart to setting
 * `offlineOnly = true` at creation time. Forces the sync state back to [SyncState.WaitingForUpload]
 * so it's picked up by the next upload pass regardless of whatever state it was left in while excluded.
 */
class SyncReminderToCloudUseCase(
  private val saveReminderUseCase: SaveReminderUseCase,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    Logger.i(TAG, "Syncing offline-only reminder to cloud, id=${reminder.uuId}")
    saveReminderUseCase(
      reminder.copy(
        offlineOnly = false,
        sync = reminder.sync.copy(syncState = SyncState.WaitingForUpload),
      ),
    )
  }

  companion object {
    private const val TAG = "SyncReminderToCloudUseCase"
  }
}

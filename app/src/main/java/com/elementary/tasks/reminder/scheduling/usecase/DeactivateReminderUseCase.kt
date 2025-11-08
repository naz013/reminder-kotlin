package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.reminder.scheduling.usecase.google.CompleteRelatedGoogleTaskUseCase
import com.elementary.tasks.reminder.scheduling.usecase.notification.UpdatePermanentReminderNotificationUseCase
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger

class DeactivateReminderUseCase(
  private val saveReminderUseCase: SaveReminderUseCase,
  private val completeRelatedGoogleTaskUseCase: CompleteRelatedGoogleTaskUseCase,
  private val pauseReminderUseCase: PauseReminderUseCase,
  private val updatePermanentReminderNotificationUseCase: UpdatePermanentReminderNotificationUseCase,
) {

  suspend operator fun invoke(reminder: Reminder): Reminder {
    Logger.d(TAG, "Deactivating reminder id=${reminder.uuId}")
    val reminder = reminder.copy(
      isActive = false,
      syncState = SyncState.WaitingForUpload,
      version = reminder.version + 1,
    )
    saveReminderUseCase(reminder)
    pauseReminderUseCase(reminder)
    updatePermanentReminderNotificationUseCase()

    if (reminder.exportToTasks) {
      completeRelatedGoogleTaskUseCase(reminder.uuId)
    }

    return reminder
  }

  companion object {
    private const val TAG = "DeactivateReminderUseCase"
  }
}

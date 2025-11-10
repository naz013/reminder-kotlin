package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.reminder.scheduling.usecase.google.CompleteRelatedGoogleTaskUseCase
import com.elementary.tasks.reminder.scheduling.usecase.notification.UpdatePermanentReminderNotificationUseCase
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.EventOccurrenceRepository

/**
 * Deactivates a reminder by setting its active state to false,
 * updating its sync state, and pausing any scheduled jobs.
 * If the reminder is exported to Google Tasks, it also completes the related task.
 *
 * @param saveReminderUseCase Use case to save the updated reminder.
 * @param completeRelatedGoogleTaskUseCase Use case to complete the related Google Task.
 * @param pauseReminderUseCase Use case to pause any scheduled jobs for the reminder.
 * @param updatePermanentReminderNotificationUseCase Use case to update permanent reminder notifications.
 */
class DeactivateReminderUseCase(
  private val saveReminderUseCase: SaveReminderUseCase,
  private val completeRelatedGoogleTaskUseCase: CompleteRelatedGoogleTaskUseCase,
  private val pauseReminderUseCase: PauseReminderUseCase,
  private val updatePermanentReminderNotificationUseCase: UpdatePermanentReminderNotificationUseCase,
  private val eventOccurrenceRepository: EventOccurrenceRepository
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

    eventOccurrenceRepository.deleteByEventId(reminder.uuId)

    return reminder
  }

  companion object {
    private const val TAG = "DeactivateReminderUseCase"
  }
}

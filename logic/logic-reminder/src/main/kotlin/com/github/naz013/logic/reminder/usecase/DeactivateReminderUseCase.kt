package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
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
  private val eventOccurrenceRepository: EventOccurrenceRepository,
) {

  @IgnorableReturnValue
  suspend operator fun invoke(reminder: ReminderV2): ReminderV2 {
    Logger.d(TAG, "Deactivating reminder id=${reminder.uuId}")
    val reminder =
      reminder.copy(
        isActive = false,
        sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
      )
    saveReminderUseCase(reminder)
    pauseReminderUseCase(reminder)
    updatePermanentReminderNotificationUseCase()

    if (reminder.taskExport != null) {
      completeRelatedGoogleTaskUseCase(reminder.uuId)
    }

    eventOccurrenceRepository.deleteByEventId(reminder.uuId)

    return reminder
  }

  companion object {
    private const val TAG = "DeactivateReminderUseCase"
  }
}

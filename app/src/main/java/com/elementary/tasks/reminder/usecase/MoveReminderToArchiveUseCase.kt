package com.elementary.tasks.reminder.usecase

import com.elementary.tasks.core.controller.EventControlFactory
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository

class MoveReminderToArchiveUseCase(
  private val eventControlFactory: EventControlFactory,
  private val reminderRepository: ReminderRepository,
  private val scheduleReminderUploadUseCase: ScheduleReminderUploadUseCase
) {

  suspend operator fun invoke(id: String) {
    val reminder = reminderRepository.getById(id) ?: run {
      Logger.w(TAG, "Reminder with id = $id not found")
      return
    }
    reminder.isRemoved = true
    reminder.version += 1
    eventControlFactory.getController(reminder).disable()
    reminderRepository.save(reminder)
    reminderRepository.updateSyncState(reminder.uuId, SyncState.WaitingForUpload)
    scheduleReminderUploadUseCase(id)
    Logger.i(TAG, "Moved reminder with id = $id to archive")
  }

  companion object {
    private const val TAG = "MoveReminderToArchiveUseCase"
  }
}

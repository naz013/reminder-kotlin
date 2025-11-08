package com.elementary.tasks.reminder.scheduling.usecase

import android.content.Context
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.reminder.scheduling.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.usecase.google.SaveReminderToGoogleCalendarUseCase
import com.elementary.tasks.reminder.scheduling.usecase.google.SaveReminderToGoogleTasksUseCase
import com.elementary.tasks.reminder.scheduling.usecase.location.StartLocationTrackingUseCase
import com.elementary.tasks.reminder.scheduling.usecase.notification.UpdatePermanentReminderNotificationUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.common.Module
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger

/**
 * Activates a reminder based on its behavior strategy.
 */
class ActivateReminderUseCase(
  private val context: Context,
  private val behaviorStrategyResolver: BehaviorStrategyResolver,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val startLocationTrackingUseCase: StartLocationTrackingUseCase,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val jobScheduler: JobScheduler,
  private val updatePermanentReminderNotificationUseCase: UpdatePermanentReminderNotificationUseCase,
  private val saveReminderToGoogleTasksUseCase: SaveReminderToGoogleTasksUseCase,
  private val saveReminderToGoogleCalendarUseCase: SaveReminderToGoogleCalendarUseCase
) {

  suspend operator fun invoke(reminder: Reminder, startAnyway: Boolean = false): Reminder {
    val strategy = behaviorStrategyResolver.resolve(reminder)

    if (strategy.requiresBackgroundService(reminder) || !strategy.requiresTimeScheduling(reminder)) {
      if (!Module.hasLocation(context)) {
        deleteReminderUseCase(reminder)
        Logger.w(TAG, "Deleting location-based reminder id=${reminder.uuId} due to missing location module.")
        return reminder
      }
      Logger.d(TAG, "Activating reminder id=${reminder.uuId} without time scheduling.")
      val reminder = reminder.copy(
        isActive = true,
        isRemoved = false,
        eventCount = reminder.eventCount + 1,
        isNotificationShown = false,
        isLocked = false,
        syncState = SyncState.WaitingForUpload,
        version = reminder.version + 1,
      )
      saveReminderUseCase(reminder)
      startLocationTrackingUseCase(reminder)
      return reminder
    }

    if (strategy.requiresTimeScheduling(reminder) && reminder.places.isNotEmpty()) {
      val reminder = reminder.copy(
        isActive = true,
        isRemoved = false,
        eventCount = reminder.eventCount + 1,
        isNotificationShown = false,
        isLocked = false,
        syncState = SyncState.WaitingForUpload,
        version = reminder.version + 1,
      )
      saveReminderUseCase(reminder)
      if (jobScheduler.scheduleGpsDelay(reminder)) {
        Logger.d(TAG, "Scheduled GPS delay for reminder id=${reminder.uuId}.")
      } else {
        Logger.d(TAG, "Starting GPS tracking immediately for reminder id=${reminder.uuId}.")
        startLocationTrackingUseCase(reminder)
      }
      return reminder
    }

    if (strategy.canStartImmediately(reminder) || startAnyway) {
      Logger.d(TAG, "Starting reminder id=${reminder.uuId} immediately.")
      val reminder = reminder.copy(
        isActive = true,
        isRemoved = false,
        eventCount = reminder.eventCount + 1,
        syncState = SyncState.WaitingForUpload,
        version = reminder.version + 1,
      )
      saveReminderUseCase(reminder)
      updatePermanentReminderNotificationUseCase()
      jobScheduler.scheduleReminder(reminder)
      saveReminderToGoogleTasksUseCase(reminder)
      saveReminderToGoogleCalendarUseCase(reminder)
      return reminder
    } else {
      Logger.w(TAG, "Cannot start reminder id=${reminder.uuId} now, outdated eventTime=${reminder.eventTime}.")
      return reminder
    }
  }

  companion object {
    private const val TAG = "ActivateReminderUseCase"
  }
}

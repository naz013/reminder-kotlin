package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.usecase.google.SaveReminderToGoogleCalendarUseCase
import com.github.naz013.logic.reminder.usecase.google.SaveReminderToGoogleTasksUseCase
import com.github.naz013.logic.reminder.work.CalculateReminderOccurrencesTask
import com.github.naz013.platform.SystemInfo
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.workapi.WorkScheduler

/**
 * Activates a reminder based on its behavior strategy.
 */
class ActivateReminderUseCase(
  private val behaviorStrategyResolver: BehaviorStrategyResolverV2,
  private val deleteReminderUseCase: DeleteReminderUseCase,
  private val startLocationTrackingUseCase: StartLocationTrackingUseCase,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val jobScheduler: JobSchedulerApi,
  private val updatePermanentReminderNotificationUseCase: UpdatePermanentReminderNotificationUseCase,
  private val saveReminderToGoogleTasksUseCase: SaveReminderToGoogleTasksUseCase,
  private val saveReminderToGoogleCalendarUseCase: SaveReminderToGoogleCalendarUseCase,
  private val workScheduler: WorkScheduler,
  private val systemInfo: SystemInfo,
) {

  @IgnorableReturnValue
  suspend operator fun invoke(
    reminder: ReminderV2,
    startAnyway: Boolean = false,
    skipGoogleTaskExport: Boolean = false,
  ): ReminderV2 {
    val strategy = behaviorStrategyResolver.resolve(reminder)

    if (strategy.requiresBackgroundService(reminder) || !strategy.requiresTimeScheduling(reminder)) {
      if (!systemInfo.hasLocation) {
        deleteReminderUseCase(reminder)
        Logger.w(TAG, "Deleting location-based reminder id=${reminder.uuId} due to missing location module.")
        return reminder
      }
      Logger.d(TAG, "Activating reminder id=${reminder.uuId} without time scheduling.")
      val reminder =
        reminder.copy(
          isActive = true,
          isRemoved = false,
          eventCount = reminder.eventCount + 1,
          location = (reminder.location ?: LocationSettings()).copy(isNotificationShown = false, isLocked = false),
          sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
        )
      saveReminderUseCase(reminder)
      startLocationTrackingUseCase(reminder)
      return reminder
    }

    if (strategy.requiresTimeScheduling(reminder) && reminder.places.isNotEmpty()) {
      val reminder =
        reminder.copy(
          isActive = true,
          isRemoved = false,
          eventCount = reminder.eventCount + 1,
          location = (reminder.location ?: LocationSettings()).copy(isNotificationShown = false, isLocked = false),
          sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
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
      val reminder =
        reminder.copy(
          isActive = true,
          isRemoved = false,
          eventCount = reminder.eventCount + 1,
          sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
        )
      saveReminderUseCase(reminder)
      updatePermanentReminderNotificationUseCase()
      jobScheduler.scheduleReminder(reminder)
      if (!skipGoogleTaskExport) {
        saveReminderToGoogleTasksUseCase(reminder)
      }
      saveReminderToGoogleCalendarUseCase(reminder)
      workScheduler.enqueue(CalculateReminderOccurrencesTask.prepareWorkRequest(reminder.uuId))
      return reminder
    } else {
      Logger.w(TAG, "Cannot start reminder id=${reminder.uuId} now, outdated eventTime=${reminder.schedule.eventDateTime}.")
      val reminder =
        reminder.copy(
          isActive = false,
          sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
        )
      saveReminderUseCase(reminder)
      return reminder
    }
  }

  companion object {
    private const val TAG = "ActivateReminderUseCase"
  }
}

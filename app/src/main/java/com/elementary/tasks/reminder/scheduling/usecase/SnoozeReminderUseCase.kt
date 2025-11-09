package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.reminder.scheduling.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.LocationBasedStrategy
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger

/**
 * Snoozes a reminder for a specified time in minutes.
 * If the reminder is location-based or requires a background service, it will not be snoozed.
 * If the snooze time is less than or equal to zero, the reminder will be completed instead.
 *
 * @param jobScheduler The job scheduler to schedule the snooze.
 * @param strategyResolver The resolver to determine the behavior strategy of the reminder.
 * @param completeReminderUseCase The use case to complete the reminder.
 * @param saveReminderUseCase The use case to save the updated reminder.
 */
class SnoozeReminderUseCase(
  private val jobScheduler: JobScheduler,
  private val strategyResolver: BehaviorStrategyResolver,
  private val completeReminderUseCase: CompleteReminderUseCase,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val notifier: Notifier
) {

  suspend operator fun invoke(reminder: Reminder, timeInMinutes: Int): Reminder {
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategy || strategy.requiresBackgroundService(reminder)) {
      Logger.w(TAG, "Cannot snooze location-based reminder id=${reminder.uuId}")
     return reminder
    }
    if (timeInMinutes <= 0) {
      Logger.w(TAG, "Snooze time is less than or equal to zero for reminder id=${reminder.uuId}")
      return completeReminderUseCase(reminder)
    }
    notifier.cancel(reminder.uniqueId)
    val reminder = reminder.copy(
      delay = timeInMinutes,
      syncState = SyncState.WaitingForUpload
    )
    saveReminderUseCase(reminder)
    jobScheduler.scheduleReminderDelay(timeInMinutes, reminder.uuId, reminder.uniqueId)
    Logger.i(TAG, "Snoozed reminder id=${reminder.uuId} for $timeInMinutes minutes")
    return reminder
  }

  companion object {
    private const val TAG = "SnoozeReminderUseCase"
  }
}

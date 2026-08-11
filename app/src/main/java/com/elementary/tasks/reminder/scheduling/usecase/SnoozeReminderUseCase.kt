package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.feature.workflow.WorkflowTriggerRunner
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.LocationBasedStrategyV2
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.scheduler.JobSchedulerApi

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
  private val jobScheduler: JobSchedulerApi,
  private val strategyResolver: BehaviorStrategyResolverV2,
  private val completeReminderUseCase: CompleteReminderUseCase,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val notifier: Notifier,
  private val workflowTriggerRunner: WorkflowTriggerRunner,
) {
  suspend operator fun invoke(
    reminder: ReminderV2,
    timeInMinutes: Int,
  ): ReminderV2 {
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategyV2 || strategy.requiresBackgroundService(reminder)) {
      Logger.w(TAG, "Cannot snooze location-based reminder id=${reminder.uuId}")
      return reminder
    }
    if (timeInMinutes <= 0) {
      Logger.w(TAG, "Snooze time is less than or equal to zero for reminder id=${reminder.uuId}")
      return completeReminderUseCase(reminder)
    }
    notifier.cancel(reminder.uniqueId)
    val reminder =
      reminder.copy(
        notification = reminder.notification.copy(delayMinutes = timeInMinutes),
        sync = reminder.sync.copy(syncState = SyncState.WaitingForUpload),
        snoozeCount = reminder.snoozeCount + 1,
        lastShownAt = null,
      )
    saveReminderUseCase(reminder)
    jobScheduler.scheduleReminderDelay(timeInMinutes, reminder.uuId, reminder.uniqueId)
    workflowTriggerRunner.onReminderSnoozed(reminder.uuId)
    Logger.i(TAG, "Snoozed reminder id=${reminder.uuId} for $timeInMinutes minutes")
    return reminder
  }

  companion object {
    private const val TAG = "SnoozeReminderUseCase"
  }
}

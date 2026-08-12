package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.LocationBasedStrategyV2
import com.github.naz013.notification.NotificationApi
import com.github.naz013.scheduler.JobSchedulerApi

/**
 * Use case to pause a reminder.
 */
class PauseReminderUseCase(
  private val notificationApi: NotificationApi,
  private val jobScheduler: JobSchedulerApi,
  private val stopLocationTrackingUseCase: StopLocationTrackingUseCase,
  private val strategyResolver: BehaviorStrategyResolverV2,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    notificationApi.cancel(reminder.uniqueId)
    jobScheduler.cancelReminder(reminder.uniqueId)
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategyV2) {
      stopLocationTrackingUseCase(reminder = reminder, isPaused = true)
    }
    Logger.i(TAG, "Paused reminder with id=${reminder.uuId}, strategy=${strategy::class.simpleName}")
  }

  companion object {
    private const val TAG = "PauseReminderUseCase"
  }
}

package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.reminder.scheduling.behavior.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.behavior.LocationBasedStrategy
import com.elementary.tasks.reminder.scheduling.usecase.location.StopLocationTrackingUseCase
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

/**
 * Use case to pause a reminder.
 */
class PauseReminderUseCase(
  private val notifier: Notifier,
  private val jobScheduler: JobScheduler,
  private val stopLocationTrackingUseCase: StopLocationTrackingUseCase,
  private val strategyResolver: BehaviorStrategyResolver
) {

  suspend operator fun invoke(reminder: Reminder) {
    notifier.cancel(reminder.uniqueId)
    jobScheduler.cancelReminder(reminder.uniqueId)
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategy) {
      stopLocationTrackingUseCase(reminder = reminder, isPaused = true)
    }
    Logger.i(TAG, "Paused reminder with id=${reminder.uuId}, strategy=${strategy::class.simpleName}")
  }

  companion object {
    private const val TAG = "PauseReminderUseCase"
  }
}

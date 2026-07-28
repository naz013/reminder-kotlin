package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.LocationBasedStrategyV2
import com.elementary.tasks.reminder.scheduling.usecase.location.StopLocationTrackingUseCase
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

/**
 * Use case to pause a reminder.
 */
class PauseReminderUseCase(
  private val notifier: Notifier,
  private val jobScheduler: JobScheduler,
  private val stopLocationTrackingUseCase: StopLocationTrackingUseCase,
  private val strategyResolver: BehaviorStrategyResolverV2,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    notifier.cancel(reminder.uniqueId)
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

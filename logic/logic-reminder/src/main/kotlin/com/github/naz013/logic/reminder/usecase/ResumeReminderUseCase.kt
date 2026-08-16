package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.LocationBasedStrategyV2
import com.github.naz013.scheduler.JobSchedulerApi

/**
 * Resumes a reminder by scheduling necessary jobs based on its strategy.
 */
class ResumeReminderUseCase(
  private val jobScheduler: JobSchedulerApi,
  private val strategyResolver: BehaviorStrategyResolverV2,
  private val startLocationTrackingUseCase: StartLocationTrackingUseCase,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    if (reminder.isActive && !reminder.isRemoved) {
      val strategy = strategyResolver.resolve(reminder)
      if (strategy is LocationBasedStrategyV2) {
        Logger.i(TAG, "Resuming location tracking for reminder id=${reminder.uuId}")
        startLocationTrackingUseCase(reminder)
      } else if (reminder.places.isNotEmpty()) {
        Logger.i(TAG, "Scheduled GPS delay for reminder id=${reminder.uuId}")
        jobScheduler.scheduleGpsDelay(reminder)
      } else {
        Logger.i(TAG, "Resuming time-based reminder id=${reminder.uuId}")
        jobScheduler.scheduleReminder(reminder)
      }
    }
  }

  companion object {
    private const val TAG = "ResumeReminderUseCase"
  }
}

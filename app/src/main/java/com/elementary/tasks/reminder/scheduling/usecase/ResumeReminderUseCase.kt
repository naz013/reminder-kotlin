package com.elementary.tasks.reminder.scheduling.usecase

import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.LocationBasedStrategyV2
import com.elementary.tasks.reminder.scheduling.usecase.location.StartLocationTrackingUseCase
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

/**
 * Resumes a reminder by scheduling necessary jobs based on its strategy.
 */
class ResumeReminderUseCase(
  private val jobScheduler: JobScheduler,
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

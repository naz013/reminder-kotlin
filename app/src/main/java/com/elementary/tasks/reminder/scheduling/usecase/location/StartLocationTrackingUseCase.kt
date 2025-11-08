package com.elementary.tasks.reminder.scheduling.usecase.location

import android.content.Context
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.reminder.scheduling.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.LocationBasedStrategy
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

/**
 * Starts location tracking if the reminder requires it.
 */
class StartLocationTrackingUseCase(
  private val context: Context,
  private val strategyResolver: BehaviorStrategyResolver
) {

  suspend operator fun invoke(reminder: Reminder) {
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategy) {
      SuperUtil.startGpsTracking(context)
      Logger.i(TAG, "Location tracking started for reminder id=${reminder.uuId}")
    }
  }

  companion object {
    private const val TAG = "StartLocationTrackingUseCase"
  }
}

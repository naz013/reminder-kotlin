package com.elementary.tasks.reminder.scheduling.usecase.location

import android.content.Context
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.LocationBasedStrategyV2
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

/**
 * Starts location tracking if the reminder requires it.
 */
class StartLocationTrackingUseCase(
  private val context: Context,
  private val strategyResolver: BehaviorStrategyResolverV2,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategyV2) {
      SuperUtil.startGpsTracking(context)
      Logger.i(TAG, "Location tracking started for reminder id=${reminder.uuId}")
    }
  }

  companion object {
    private const val TAG = "StartLocationTrackingUseCase"
  }
}

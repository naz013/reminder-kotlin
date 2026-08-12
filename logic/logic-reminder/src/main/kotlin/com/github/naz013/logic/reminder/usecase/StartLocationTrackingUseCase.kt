package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.location.LocationTrackingApi
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.LocationBasedStrategyV2

/**
 * Starts location tracking if the reminder requires it.
 */
class StartLocationTrackingUseCase(
  private val locationTrackingApi: LocationTrackingApi,
  private val strategyResolver: BehaviorStrategyResolverV2,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategyV2) {
      locationTrackingApi.startTracking()
      Logger.i(TAG, "Location tracking started for reminder id=${reminder.uuId}")
    }
  }

  companion object {
    private const val TAG = "StartLocationTrackingUseCase"
  }
}

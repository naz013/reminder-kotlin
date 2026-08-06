package com.elementary.tasks.reminder.scheduling.usecase.location

import android.content.Context
import com.elementary.tasks.core.services.GeolocationService
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderV2Repository

/**
 * Checks if there are any active geolocation reminders left.
 * If not, stops the [GeolocationService].
 */
class StopLocationTrackingUseCase(
  private val context: Context,
  private val reminderV2Repository: ReminderV2Repository,
) {
  suspend operator fun invoke(
    reminder: ReminderV2,
    isPaused: Boolean,
  ) {
    val list =
      reminderV2Repository.getAll(active = true, removed = false).filter { it.places.isNotEmpty() }
    if (list.isEmpty()) {
      SuperUtil.stopService(context, GeolocationService::class.java)
      Logger.i(TAG, "No active geolocation reminders. Stopping service.")
      return
    }
    var hasActive = false
    for (item in list) {
      if (isPaused && item.uniqueId == reminder.uniqueId) {
        continue
      }
      if (!(item.location?.isNotificationShown ?: false)) {
        hasActive = true
        break
      }
    }
    if (!hasActive) {
      SuperUtil.stopService(context, GeolocationService::class.java)
      Logger.i(TAG, "No active geolocation reminders. Stopping service.")
    } else {
      Logger.i(TAG, "There are still active geolocation reminders. Service will continue.")
    }
  }

  companion object {
    private const val TAG = "StopLocationTrackingUseCase"
  }
}

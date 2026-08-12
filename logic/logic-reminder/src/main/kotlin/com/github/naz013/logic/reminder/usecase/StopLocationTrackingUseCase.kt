package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.location.LocationTrackingApi
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderV2Repository

class StopLocationTrackingUseCase(
  private val locationTrackingApi: LocationTrackingApi,
  private val reminderV2Repository: ReminderV2Repository,
) {
  suspend operator fun invoke(
    reminder: ReminderV2,
    isPaused: Boolean,
  ) {
    val list =
      reminderV2Repository.getAll(active = true, removed = false).filter { it.places.isNotEmpty() }
    if (list.isEmpty()) {
      locationTrackingApi.stopTracking()
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
      locationTrackingApi.stopTracking()
      Logger.i(TAG, "No active geolocation reminders. Stopping service.")
    } else {
      Logger.i(TAG, "There are still active geolocation reminders. Service will continue.")
    }
  }

  companion object {
    private const val TAG = "StopLocationTrackingUseCase"
  }
}

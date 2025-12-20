package com.elementary.tasks.reminder.scheduling.usecase.location

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.core.services.GeolocationService
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderRepository

/**
 * Checks if there are any active geolocation reminders left.
 * If not, stops the [GeolocationService].
 */
class StopLocationTrackingUseCase(
  private val context: Context,
  private val reminderRepository: ReminderRepository,
  private val dateTimeManager: DateTimeManager
) {

  suspend operator fun invoke(reminder: Reminder, isPaused: Boolean) {
    val list = reminderRepository.getAllTypes(
      active = true,
      removed = false,
      types = Reminder.gpsTypes()
    )
    if (list.isEmpty()) {
      SuperUtil.stopService(context, GeolocationService::class.java)
      Logger.i(TAG, "No active geolocation reminders. Stopping service.")
      return
    }
    var hasActive = false
    for (item in list) {
      if (isPaused) {
        if (item.uniqueId == reminder.uniqueId) {
          continue
        }
        if (TextUtils.isEmpty(item.eventTime) || !dateTimeManager.isCurrent(item.eventTime)) {
          if (!item.isNotificationShown) {
            hasActive = true
            break
          }
        } else {
          if (!item.isNotificationShown) {
            hasActive = true
            break
          }
        }
      } else {
        if (!item.isNotificationShown) {
          hasActive = true
          break
        }
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

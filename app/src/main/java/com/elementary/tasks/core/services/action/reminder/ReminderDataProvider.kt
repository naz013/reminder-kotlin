package com.elementary.tasks.core.services.action.reminder

import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.params.Prefs

class ReminderDataProvider(
  private val textProvider: TextProvider,
  private val prefs: Prefs
) {

  fun getLedColor(reminderColor: Int): Int? {
    return if (Module.isPro && prefs.isLedEnabled) {
      if (reminderColor != -1) {
        reminderColor
      } else {
        LED.getLED(prefs.ledColor)
      }
    } else {
      return null
    }
  }

  fun getVibrationPattern(): LongArray? {
    return if (prefs.isVibrateEnabled) {
      if (prefs.isInfiniteVibrateEnabled) {
        longArrayOf(150, 86400000)
      } else {
        longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
      }
    } else {
      null
    }
  }

  fun getAppName(): String {
    return if (Module.isPro) {
      textProvider.getText(R.string.app_name_pro)
    } else {
      textProvider.getText(R.string.app_name)
    }
  }

  fun priority(priority: Int): Int {
    return when (priority) {
      0 -> NotificationCompat.PRIORITY_MIN
      1 -> NotificationCompat.PRIORITY_LOW
      2 -> NotificationCompat.PRIORITY_DEFAULT
      3 -> NotificationCompat.PRIORITY_HIGH
      else -> NotificationCompat.PRIORITY_MAX
    }
  }
}

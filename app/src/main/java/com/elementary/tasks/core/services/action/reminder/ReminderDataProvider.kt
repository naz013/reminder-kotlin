package com.elementary.tasks.core.services.action.reminder

import androidx.core.app.NotificationCompat
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.TextProvider

class ReminderDataProvider(
  private val textProvider: TextProvider,
  private val prefs: Prefs
) {

  fun getLedColor(reminderColor: Int): Int? {
    return if (BuildParams.isPro && prefs.isLedEnabled) {
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
    return longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
  }

  fun getAppName(): String {
    return textProvider.getAppName()
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

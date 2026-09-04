package com.elementary.tasks.core.services.action.reminder

import androidx.core.app.NotificationCompat
import com.github.naz013.feature.reminder.util.LED
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo

class ReminderDataProvider(
  private val textProvider: TextProvider,
  private val prefs: Prefs,
  private val buildInfo: BuildInfo,
) {
  fun getLedColor(reminderColor: Int): Int? {
    return if (buildInfo.isPro && prefs.isLedEnabled) {
      if (reminderColor != -1) {
        reminderColor
      } else {
        LED.getLED(prefs.ledColor, buildInfo.isPro)
      }
    } else {
      return null
    }
  }

  fun getAppName(): String = textProvider.getAppName()

  fun priority(priority: Int): Int =
    when (priority) {
      0 -> NotificationCompat.PRIORITY_MIN
      1 -> NotificationCompat.PRIORITY_LOW
      2 -> NotificationCompat.PRIORITY_DEFAULT
      3 -> NotificationCompat.PRIORITY_HIGH
      else -> NotificationCompat.PRIORITY_MAX
    }
}

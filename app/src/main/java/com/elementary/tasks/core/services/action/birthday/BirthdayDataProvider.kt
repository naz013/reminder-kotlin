package com.elementary.tasks.core.services.action.birthday

import androidx.core.app.NotificationCompat
import com.github.naz013.feature.reminder.util.LED
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo

class BirthdayDataProvider(
  private val textProvider: TextProvider,
  private val prefs: Prefs,
  private val buildInfo: BuildInfo,
) {
  fun priority(priority: Int): Int =
    when (priority) {
      0 -> NotificationCompat.PRIORITY_MIN
      1 -> NotificationCompat.PRIORITY_LOW
      2 -> NotificationCompat.PRIORITY_DEFAULT
      3 -> NotificationCompat.PRIORITY_HIGH
      else -> NotificationCompat.PRIORITY_MAX
    }

  fun getLedColor(): Int {
    var ledColor = LED.getLED(prefs.ledColor, buildInfo.isPro)
    if (buildInfo.isPro && !prefs.isBirthdayGlobalEnabled) {
      ledColor = LED.getLED(prefs.birthdayLedColor, buildInfo.isPro)
    }
    return ledColor
  }

  fun getVibrationPattern(): LongArray? = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)

  fun getAppName(): String = textProvider.getAppName()

  fun isBirthdayLed(): Boolean =
    if (prefs.isBirthdayGlobalEnabled) {
      prefs.isLedEnabled
    } else {
      prefs.isBirthdayLedEnabled
    }
}

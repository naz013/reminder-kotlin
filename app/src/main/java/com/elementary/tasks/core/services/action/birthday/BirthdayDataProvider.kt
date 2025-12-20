package com.elementary.tasks.core.services.action.birthday

import androidx.core.app.NotificationCompat
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.TextProvider

class BirthdayDataProvider(
  private val textProvider: TextProvider,
  private val prefs: Prefs
) {

  fun priority(priority: Int): Int {
    return when (priority) {
      0 -> NotificationCompat.PRIORITY_MIN
      1 -> NotificationCompat.PRIORITY_LOW
      2 -> NotificationCompat.PRIORITY_DEFAULT
      3 -> NotificationCompat.PRIORITY_HIGH
      else -> NotificationCompat.PRIORITY_MAX
    }
  }

  fun getLedColor(): Int {
    var ledColor = LED.getLED(prefs.ledColor)
    if (BuildParams.isPro && !prefs.isBirthdayGlobalEnabled) {
      ledColor = LED.getLED(prefs.birthdayLedColor)
    }
    return ledColor
  }

  fun getVibrationPattern(): LongArray? {
    return longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
  }

  fun getAppName(): String {
    return textProvider.getAppName()
  }

  fun isBirthdayLed(): Boolean {
    return if (prefs.isBirthdayGlobalEnabled) {
      prefs.isLedEnabled
    } else {
      prefs.isBirthdayLedEnabled
    }
  }
}

package com.elementary.tasks.core.services.action.birthday

import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.params.Prefs

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
    if (Module.isPro && !prefs.isBirthdayGlobalEnabled) {
      ledColor = LED.getLED(prefs.birthdayLedColor)
    }
    return ledColor
  }

  fun getVibrationPattern(): LongArray? {
    return if (isBirthdayVibration()) {
      if (isBirthdayVibrationInfinite()) {
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

  private fun isBirthdayVibration(): Boolean {
    return if (prefs.isBirthdayGlobalEnabled) {
      prefs.isVibrateEnabled
    } else {
      prefs.isBirthdayVibrationEnabled
    }
  }

  private fun isBirthdayVibrationInfinite(): Boolean {
    return if (prefs.isBirthdayGlobalEnabled) {
      prefs.isInfiniteVibrateEnabled
    } else {
      prefs.isBirthdayInfiniteVibrationEnabled
    }
  }

  fun isBirthdayLed(): Boolean {
    return if (prefs.isBirthdayGlobalEnabled) {
      prefs.isLedEnabled
    } else {
      prefs.isBirthdayLedEnabled
    }
  }
}

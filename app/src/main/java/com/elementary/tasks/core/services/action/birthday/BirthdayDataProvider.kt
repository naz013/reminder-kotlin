package com.elementary.tasks.core.services.action.birthday

import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.params.Prefs

class BirthdayDataProvider(
  private val textProvider: TextProvider,
  private val contextProvider: ContextProvider,
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

  fun getSound(): Uri {
    return ReminderUtils.getSound(contextProvider.context, birthdayMelody(), "").uri.apply {
      contextProvider.context.grantUriPermission(
        "com.android.systemui",
        this,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
      )
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

  fun isBirthdaySilentEnabled(): Boolean {
    return if (prefs.isBirthdayGlobalEnabled) {
      prefs.isSoundInSilentModeEnabled
    } else {
      prefs.isBirthdaySilentEnabled
    }
  }

  fun isBirthdayLed(): Boolean {
    return if (prefs.isBirthdayGlobalEnabled) {
      prefs.isLedEnabled
    } else {
      prefs.isBirthdayLedEnabled
    }
  }

  private fun birthdayMelody(): String {
    return if (prefs.isBirthdayGlobalEnabled) {
      prefs.melodyFile
    } else {
      prefs.birthdayMelody
    }
  }
}

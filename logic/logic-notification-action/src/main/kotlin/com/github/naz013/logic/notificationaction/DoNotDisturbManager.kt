package com.github.naz013.logic.notificationaction

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.logging.Logger

class DoNotDisturbManager(
  private val preferences: DoNotDisturbPreferences,
  private val dateTimeManager: DateTimeManager,
) {
  fun applyDoNotDisturb(
    priority: Int,
    millis: Long = System.currentTimeMillis(),
  ): Boolean {
    if (preferences.isDoNotDisturbEnabled) {
      val range = dateTimeManager.doNotDisturbRange(preferences.doNotDisturbFrom, preferences.doNotDisturbTo)
      return if (millis in range) {
        if (preferences.doNotDisturbIgnore == 5) {
          Logger.i(TAG, "Do not disturb active: ignoring all.")
          true
        } else {
          (priority < preferences.doNotDisturbIgnore).also {
            Logger.i(
              TAG,
              "Do not disturb active: priority check. Task priority: $priority, ignore level: " +
                "${preferences.doNotDisturbIgnore}, should ignore: $it",
            )
          }
        }
      } else {
        false
      }
    }
    return false
  }

  companion object {
    private const val TAG = "DoNotDisturbManager"
  }
}

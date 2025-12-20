package com.elementary.tasks.core.utils.datetime

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logging.Logger
import com.github.naz013.common.datetime.DateTimeManager

class DoNotDisturbManager(
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager
) {

  fun applyDoNotDisturb(priority: Int, millis: Long = System.currentTimeMillis()): Boolean {
    if (prefs.isDoNotDisturbEnabled) {
      val range = dateTimeManager.doNotDisturbRange(prefs.doNotDisturbFrom, prefs.doNotDisturbTo)
      return if (millis in range) {
        if (prefs.doNotDisturbIgnore == 5) {
          Logger.i(TAG, "Do not disturb active: ignoring all.")
          true
        } else {
          (priority < prefs.doNotDisturbIgnore).also {
            Logger.i(
              TAG,
              "Do not disturb active: priority check. Task priority: $priority, ignore level: ${prefs.doNotDisturbIgnore}, should ignore: $it"
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

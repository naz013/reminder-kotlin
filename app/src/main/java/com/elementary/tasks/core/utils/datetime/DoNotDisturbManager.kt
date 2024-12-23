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
      Logger.d("applyDoNotDisturb: enabled, $millis")
      val range = dateTimeManager.doNotDisturbRange(prefs.doNotDisturbFrom, prefs.doNotDisturbTo)
      return if (range.contains(millis)) {
        if (prefs.doNotDisturbIgnore == 5) {
          true
        } else {
          priority < prefs.doNotDisturbIgnore
        }
      } else {
        false
      }
    }
    return false
  }
}

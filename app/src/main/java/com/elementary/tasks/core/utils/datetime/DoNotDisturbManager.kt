package com.elementary.tasks.core.utils.datetime

import com.elementary.tasks.core.utils.params.Prefs
import timber.log.Timber

class DoNotDisturbManager(
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager
) {

  fun applyDoNotDisturb(priority: Int, millis: Long = System.currentTimeMillis()): Boolean {
    if (prefs.isDoNotDisturbEnabled) {
      Timber.d("applyDoNotDisturb: enabled, $millis")
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

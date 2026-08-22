package com.github.naz013.logic.notificationaction

/**
 * Seam over the do-not-disturb subset of app's monolithic `Prefs`. Implemented in `app` by
 * wrapping `Prefs` - see `DoNotDisturbPreferencesImpl`.
 */
interface DoNotDisturbPreferences {
  val isDoNotDisturbEnabled: Boolean
  val doNotDisturbFrom: String
  val doNotDisturbTo: String
  val doNotDisturbIgnore: Int
  val doNotDisturbAction: Int
}

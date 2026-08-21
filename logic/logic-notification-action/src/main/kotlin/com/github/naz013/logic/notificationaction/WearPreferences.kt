package com.github.naz013.logic.notificationaction

/**
 * Seam over the wear-companion-notification toggle in app's monolithic `Prefs`. Implemented in
 * `app` by wrapping `Prefs` - see `WearPreferencesImpl`.
 */
interface WearPreferences {
  val isWearEnabled: Boolean
}

package com.github.naz013.logic.notificationaction

/**
 * Seam over the in-app-alert-banner toggle in app's monolithic `Prefs`, shared by both the
 * reminder- and birthday-side processors. Implemented in `app` by wrapping `Prefs` - see
 * `InAppAlertPreferencesImpl`.
 */
interface InAppAlertPreferences {
  val isInAppAlertBannerEnabled: Boolean
}

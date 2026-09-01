package com.github.naz013.feature.settings.digest

/**
 * `feature-settings` never touches `app`'s `Prefs` directly - this small seam (implemented in
 * `app`, same shape as `CalendarSettingsPreferences`) is what the Digest settings screen reads
 * and writes instead.
 */
interface DigestSettingsPreferences {
  var aiDigestDailyEnabled: Boolean
  var aiDigestHour: Int
}

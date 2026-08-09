package com.github.naz013.holidaysapi

/**
 * The single seam the public holiday feature uses to ask "is this feature currently allowed to
 * run" and "for which country" - implemented in `app` on top of `FeatureManager`/`Prefs`, so this
 * module never depends on either directly.
 */
interface HolidaySettingsGate {
  /** True only when both the remote kill-switch and the user's own toggle are on. */
  fun isEnabled(): Boolean

  /** ISO 3166-1 alpha-2 country code the user selected. */
  fun countryCode(): String
}

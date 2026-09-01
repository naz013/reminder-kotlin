package com.github.naz013.digestapi

/**
 * The single seam the AI digest feature uses to ask "is the daily digest currently allowed to run"
 * and "at what hour" - implemented in `app` on top of `FeatureFlags`/`Prefs`, so this module never
 * depends on either directly.
 */
interface DigestSettingsGate {
  /** True only when both the remote kill-switch and the user's own toggle are on. */
  fun isDailyEnabled(): Boolean

  /** Local hour (0-23) the user wants the digest posted at or after. */
  fun preferredHour(): Int
}

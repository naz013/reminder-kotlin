package com.github.naz013.featureflags

/**
 * Answers "is this feature currently enabled" without exposing where flags come from
 * (SharedPreferences, remote config, or anything else) - implemented on top of that storage
 * in `app`, so any module can depend on this interface to read flags without depending on `app`.
 */
interface FeatureFlags {
  fun isEnabled(feature: FeatureFlag): Boolean
}

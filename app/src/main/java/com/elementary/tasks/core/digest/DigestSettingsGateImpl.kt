package com.elementary.tasks.core.digest

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.digestapi.DigestSettingsGate
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags

/**
 * Deliberately does not check [com.github.naz013.common.system.BuildInfo.isPro] - that's a
 * separate, purely presentational concern for the settings UI (lock/upsell state). This answers
 * "should background work actually run," which must go false the instant the user's own toggle
 * is off, regardless of flavor - see research/AI_DAILY_DIGEST_PLAN.md ("Gating chain").
 */
class DigestSettingsGateImpl(
  private val featureFlags: FeatureFlags,
  private val prefs: Prefs,
) : DigestSettingsGate {
  override fun isDailyEnabled(): Boolean =
    featureFlags.isEnabled(FeatureFlag.AI_DIGEST) && prefs.aiDigestDailyEnabled

  override fun preferredHour(): Int = prefs.aiDigestHour
}

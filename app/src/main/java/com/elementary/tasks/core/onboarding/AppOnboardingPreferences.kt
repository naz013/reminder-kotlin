package com.elementary.tasks.core.onboarding

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.onboarding.OnboardingPreferences

/**
 * `feature-onboarding` can't depend on `app`, so this wraps `Prefs.hasSeenOnboarding` behind
 * [OnboardingPreferences] instead.
 */
class AppOnboardingPreferences(
  private val prefs: Prefs,
) : OnboardingPreferences {
  override var hasSeenOnboarding: Boolean
    get() = prefs.hasSeenOnboarding
    set(value) { prefs.hasSeenOnboarding = value }
}

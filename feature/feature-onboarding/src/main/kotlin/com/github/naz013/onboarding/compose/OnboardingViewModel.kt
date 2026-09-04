package com.github.naz013.onboarding.compose

import androidx.lifecycle.ViewModel
import com.github.naz013.onboarding.OnboardingPreferences

class OnboardingViewModel(
  private val onboardingPreferences: OnboardingPreferences,
) : ViewModel() {

  fun onOnboardingDismissed() {
    onboardingPreferences.hasSeenOnboarding = true
  }
}

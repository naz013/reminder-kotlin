package com.github.naz013.onboarding

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface OnboardingNavKey : NavKey {
  @Serializable
  data object Main : OnboardingNavKey
}

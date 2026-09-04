package com.github.naz013.onboarding

import com.github.naz013.onboarding.compose.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule = module {
  viewModelOf(::OnboardingViewModel)
}

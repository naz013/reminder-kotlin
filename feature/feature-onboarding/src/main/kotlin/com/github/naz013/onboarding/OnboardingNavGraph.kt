package com.github.naz013.onboarding

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.onboarding.compose.OnboardingScreen
import com.github.naz013.onboarding.compose.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.onboardingEntries(
  backStack: MutableList<NavKey>,
  onFinished: () -> Unit,
  onCreateFirstReminder: () -> Unit,
) {
  entry<OnboardingNavKey.Main> {
    OnboardingMainEntry(onFinished = onFinished, onCreateFirstReminder = onCreateFirstReminder)
  }
}

@Composable
private fun OnboardingMainEntry(
  onFinished: () -> Unit,
  onCreateFirstReminder: () -> Unit,
) {
  val viewModel = koinViewModel<OnboardingViewModel>()

  OnboardingScreen(
    onSkip = {
      viewModel.onOnboardingDismissed()
      onFinished()
    },
    onCreateFirstReminder = {
      viewModel.onOnboardingDismissed()
      onCreateFirstReminder()
    },
  )
}

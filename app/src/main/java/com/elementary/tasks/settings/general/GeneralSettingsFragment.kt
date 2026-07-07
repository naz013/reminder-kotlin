package com.elementary.tasks.settings.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.splash.SplashScreenActivity
import com.github.naz013.ui.common.activity.finishWith
import com.google.android.material.color.DynamicColors
import org.koin.androidx.viewmodel.ext.android.viewModel

class GeneralSettingsFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<GeneralSettingsViewModel>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }

    GeneralSettingsScreen(
      state = state,
      onLanguageClick = viewModel::onLanguageClick,
      onThemeClick = viewModel::onThemeClick,
      onTimeFormatClick = viewModel::onTimeFormatClick,
      onDynamicColorsToggle = { viewModel.onDynamicColorsToggle() },
      onMetricToggle = { viewModel.onMetricToggle() },
      onAnalyticsToggle = { viewModel.onAnalyticsToggle() },
      onDialogOptionSelected = viewModel::onDialogOptionSelected,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  private fun handleEvent(event: GeneralSettingsEvent) {
    when (event) {
      GeneralSettingsEvent.RecreateActivity -> activity?.recreate()

      GeneralSettingsEvent.ApplyDynamicColorsAndRecreate -> {
        activity?.let {
          if (prefs.useDynamicColors) {
            DynamicColors.applyToActivityIfAvailable(it)
          }
          it.recreate()
        }
      }

      GeneralSettingsEvent.RestartApp -> activity?.finishWith(SplashScreenActivity::class.java)
    }
  }

  override fun getTitle(): String = getString(R.string.general)
}

package com.github.naz013.feature.settings.digest

import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.digestapi.DigestScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class DigestSettingsViewModel(
  private val prefs: DigestSettingsPreferences,
  private val digestScheduler: DigestScheduler,
  analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {
  val state: StateFlow<DigestSettingsState> field = MutableStateFlow(buildState())

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.AI_DIGEST_SETTINGS))
  }

  fun onDailyToggle() {
    val newValue = !prefs.aiDigestDailyEnabled
    prefs.aiDigestDailyEnabled = newValue
    if (newValue) {
      digestScheduler.enable()
    } else {
      digestScheduler.disable()
    }
    refreshState()
  }

  fun onHourSelected(hour: Int) {
    prefs.aiDigestHour = hour
    refreshState()
  }

  private fun refreshState() {
    state.update { buildState() }
  }

  private fun buildState(): DigestSettingsState =
    DigestSettingsState(
      isDailyEnabled = prefs.aiDigestDailyEnabled,
      hour = prefs.aiDigestHour,
    )
}

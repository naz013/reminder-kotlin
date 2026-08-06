package com.elementary.tasks.settings.general

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.ui.common.locale.Language
import com.github.naz013.ui.common.theme.ThemeModeHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class GeneralSettingsViewModel(
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val themeModeHolder: ThemeModeHolder,
) : ViewModel() {
  val state: StateFlow<GeneralSettingsState> field = MutableStateFlow(buildState())
  val event: LiveData<Event<GeneralSettingsEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.GENERAL_SETTINGS))
  }

  fun onLanguageClick() {
    val options = languageOptions()
    showDialog(
      GeneralSettingsDialog.Language(
        title = textProvider.getString(R.string.application_language),
        options = options,
        selectedIndex = prefs.appLanguage.coerceIn(options.indices),
      ),
    )
  }

  fun onThemeClick() {
    val options = themeOptions()
    showDialog(
      GeneralSettingsDialog.Theme(
        title = textProvider.getString(R.string.theme),
        options = options,
        selectedIndex = themeIndexFor(prefs.nightMode),
      ),
    )
  }

  fun onTimeFormatClick() {
    val options = timeFormatOptions()
    showDialog(
      GeneralSettingsDialog.TimeFormat(
        title = textProvider.getString(R.string._24_hour_time_format),
        options = options,
        selectedIndex = prefs.hourFormat.coerceIn(options.indices),
      ),
    )
  }

  fun onDialogOptionSelected(index: Int) {
    when (state.value.dialog) {
      is GeneralSettingsDialog.Language -> selectLanguage(index)
      is GeneralSettingsDialog.Theme -> selectTheme(index)
      is GeneralSettingsDialog.TimeFormat -> selectTimeFormat(index)
      null -> Unit
    }
    dismissDialog()
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  fun onMetricToggle() {
    prefs.useMetric = !prefs.useMetric
    refreshState()
  }

  fun onHapticToggle() {
    prefs.hapticsEnabled = !prefs.hapticsEnabled
    refreshState()
    if (prefs.hapticsEnabled) {
      event.emit(GeneralSettingsEvent.HapticFeedback)
    }
  }

  fun onAnalyticsToggle() {
    prefs.analyticsEnabled = !prefs.analyticsEnabled
    analyticsEventSender.setCollectionEnabled(prefs.analyticsEnabled)
    refreshState()
  }

  private fun selectLanguage(index: Int) {
    val changed = prefs.appLanguage != index
    prefs.appLanguage = index
    refreshState()
    if (changed) {
      AppCompatDelegate.setApplicationLocales(Language.getLocaleList(index))
      event.emit(GeneralSettingsEvent.RestartApp)
    }
  }

  private fun selectTheme(index: Int) {
    val mode = nightModeFor(index)
    prefs.nightMode = mode
    themeModeHolder.nightMode = mode
    refreshState()
  }

  private fun selectTimeFormat(index: Int) {
    prefs.hourFormat = index
    refreshState()
  }

  private fun showDialog(dialog: GeneralSettingsDialog) {
    state.update { it.copy(dialog = dialog) }
  }

  private fun dismissDialog() {
    state.update { it.copy(dialog = null) }
  }

  private fun refreshState() {
    state.update { buildState() }
  }

  private fun buildState(): GeneralSettingsState {
    val languageOptions = languageOptions()
    val themeOptions = themeOptions()
    val timeFormatOptions = timeFormatOptions()
    return GeneralSettingsState(
      languageName = languageOptions[prefs.appLanguage.coerceIn(languageOptions.indices)],
      themeName = themeOptions[themeIndexFor(prefs.nightMode)],
      timeFormatName = timeFormatOptions[prefs.hourFormat.coerceIn(timeFormatOptions.indices)],
      isMetricChecked = prefs.useMetric,
      isAnalyticsChecked = prefs.analyticsEnabled,
      hapticFeedbackEnabled = prefs.hapticsEnabled,
    )
  }

  private fun languageOptions(): List<String> = textProvider.getStringArray(R.array.app_languages).toList()

  private fun themeOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.light),
      textProvider.getString(R.string.dark),
      textProvider.getString(R.string.system_default),
    )

  private fun timeFormatOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.system_default),
      textProvider.getString(R.string.use_24_hour_format),
      textProvider.getString(R.string.use_12_hour_format),
    )

  private fun themeIndexFor(nightMode: Int): Int =
    when (nightMode) {
      AppCompatDelegate.MODE_NIGHT_NO -> 0
      AppCompatDelegate.MODE_NIGHT_YES -> 1
      else -> 2
    }

  private fun nightModeFor(index: Int): Int =
    when (index) {
      0 -> AppCompatDelegate.MODE_NIGHT_NO
      1 -> AppCompatDelegate.MODE_NIGHT_YES
      else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}

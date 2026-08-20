package com.github.naz013.feature.settings.general

internal data class GeneralSettingsState(
  val languageName: String = "",
  val themeName: String = "",
  val timeFormatName: String = "",
  val isMetricChecked: Boolean = false,
  val isAnalyticsChecked: Boolean = false,
  val dialog: GeneralSettingsDialog? = null,
  val hapticFeedbackEnabled: Boolean = false,
)

internal sealed class GeneralSettingsDialog {
  abstract val title: String
  abstract val options: List<String>
  abstract val selectedIndex: Int

  data class Language(
    override val title: String,
    override val options: List<String>,
    override val selectedIndex: Int,
  ) : GeneralSettingsDialog()

  data class Theme(
    override val title: String,
    override val options: List<String>,
    override val selectedIndex: Int,
  ) : GeneralSettingsDialog()

  data class TimeFormat(
    override val title: String,
    override val options: List<String>,
    override val selectedIndex: Int,
  ) : GeneralSettingsDialog()
}

internal sealed interface GeneralSettingsEvent {
  data object RecreateActivity : GeneralSettingsEvent

  data object RestartApp : GeneralSettingsEvent

  data object HapticFeedback : GeneralSettingsEvent
}

package com.elementary.tasks.settings.general

data class GeneralSettingsState(
  val languageName: String = "",
  val themeName: String = "",
  val timeFormatName: String = "",
  val isDynamicColorsVisible: Boolean = false,
  val isMetricChecked: Boolean = false,
  val isAnalyticsChecked: Boolean = false,
  val dialog: GeneralSettingsDialog? = null,
  val useDynamicColors: Boolean = false,
  val hapticFeedbackEnabled: Boolean = false,
)

sealed class GeneralSettingsDialog {
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

sealed interface GeneralSettingsEvent {
  data object RecreateActivity : GeneralSettingsEvent

  data object RestartApp : GeneralSettingsEvent

  data class ApplyDynamicColorsAndRecreate(
    val useDynamicColors: Boolean,
  ) : GeneralSettingsEvent

  data object HapticFeedback : GeneralSettingsEvent
}

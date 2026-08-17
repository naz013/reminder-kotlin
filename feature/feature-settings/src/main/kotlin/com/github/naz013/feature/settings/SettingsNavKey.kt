package com.github.naz013.feature.settings

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface SettingsNavKey : NavKey {
  @Serializable
  data object Hub : SettingsNavKey

  @Serializable
  data object General : SettingsNavKey

  @Serializable
  data object Backup : SettingsNavKey

  @Serializable
  data class Reminders(
    val screenTitle: String? = null,
  ) : SettingsNavKey

  @Serializable
  data class Calendar(
    val screenTitle: String? = null,
  ) : SettingsNavKey

  @Serializable
  data object SelectHolidayCountry : SettingsNavKey

  @Serializable
  data class Birthday(
    val screenTitle: String? = null,
  ) : SettingsNavKey

  @Serializable
  data class Note(
    val screenTitle: String? = null,
  ) : SettingsNavKey

  @Serializable
  data object ManagePresets : SettingsNavKey

  @Serializable
  data object Developer : SettingsNavKey

  @Serializable
  data object ObjectExportTest : SettingsNavKey

  @Serializable
  data object ProVersion : SettingsNavKey

  @Serializable
  data object Troubleshooting : SettingsNavKey

  @Serializable
  data object NotificationCustomizationHelp : SettingsNavKey
}

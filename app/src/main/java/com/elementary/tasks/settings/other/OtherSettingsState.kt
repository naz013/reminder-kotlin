package com.elementary.tasks.settings.other

data class OtherSettingsState(
  val aboutDialog: AboutDialogState? = null,
)

data class AboutDialogState(
  val appName: String,
  val version: String,
  val translators: String,
)

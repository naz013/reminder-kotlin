package com.elementary.tasks.settings.other

data class OtherSettingsState(
  val aboutDialog: AboutDialogState? = null,
  val permissionItems: List<PermissionItem> = emptyList(),
  val isGeminiFunctionsVisible: Boolean = false,
  val isGeminiFunctionsLocked: Boolean = false,
)

data class AboutDialogState(
  val appName: String,
  val version: String,
  val translators: String,
)

data class PermissionItem(val title: String, val permission: String)

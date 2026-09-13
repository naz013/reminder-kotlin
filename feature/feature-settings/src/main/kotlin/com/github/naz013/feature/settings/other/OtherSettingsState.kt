package com.github.naz013.feature.settings.other

internal data class OtherSettingsState(
  val permissionItems: List<PermissionItem> = emptyList(),
  val isGeminiFunctionsVisible: Boolean = false,
  val isGeminiFunctionsLocked: Boolean = false,
  val isBuyMeACoffeeVisible: Boolean = false,
  val isDigestVisible: Boolean = false,
  val isDigestLocked: Boolean = false,
)

internal data class PermissionItem(val title: String, val permission: String)

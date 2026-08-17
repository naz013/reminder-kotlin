package com.github.naz013.feature.settings

data class SettingsHubState(
  val saleMessage: String? = null,
  val updateMessage: String? = null,
  val internalMessage: String? = null,
  val isDoNotDisturbActive: Boolean = false,
  val isBuyProBadgeVisible: Boolean = false,
  val isPlayServicesWarningVisible: Boolean = false,
  val hasPinCode: Boolean = false,
  val isDeveloperOptionVisible: Boolean = false,
)

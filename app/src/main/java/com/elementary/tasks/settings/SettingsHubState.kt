package com.elementary.tasks.settings

data class SettingsHubState(
  val saleMessage: String? = null,
  val updateMessage: String? = null,
  val internalMessage: String? = null,
  val isDoNotDisturbActive: Boolean = false,
  val isBuyProBadgeVisible: Boolean = false,
  val isPlayServicesWarningVisible: Boolean = false,
  val hasPinCode: Boolean = false,
  val isInsightsVisible: Boolean = false,
)

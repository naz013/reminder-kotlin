package com.elementary.tasks.settings

data class SettingsHubState(
  val saleMessage: String? = null,
  val updateMessage: String? = null,
  val internalMessage: String? = null,
  val isDoNotDisturbActive: Boolean = false,
)

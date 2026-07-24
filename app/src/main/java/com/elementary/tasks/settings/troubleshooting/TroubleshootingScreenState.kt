package com.elementary.tasks.settings.troubleshooting

data class TroubleshootingScreenState(
  val showSendLogs: Boolean = false,
  val showBatteryOptimizationCard: Boolean = false,
  val showEmptyView: Boolean = true,
)

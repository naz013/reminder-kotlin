package com.github.naz013.feature.settings.digest

internal data class DigestSettingsState(
  val isDailyEnabled: Boolean = false,
  val hour: Int = 8,
)

package com.github.naz013.feature.settings.protrial

internal data class ProTrialState(
  val isTrialActive: Boolean = false,
  /** Formatted `hh:mm` remaining time, empty while the trial is not active. */
  val remainingTime: String = "",
  val isRunning: Boolean = false,
  val adsWatched: Int = 0,
  val requiredAds: Int = 0,
  val message: ProTrialMessage? = null,
)

internal enum class ProTrialMessage {
  ADS_UNAVAILABLE,
  CANCELLED,
}

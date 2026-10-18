package com.elementary.tasks.module.platform

import android.app.Activity
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.ProTrialActivatedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class ProTrialAdsResult {
  SUCCESS,
  ADS_UNAVAILABLE,
  CANCELLED,
}

/** Shows [REQUIRED_ADS] rewarded ads in a row and activates the trial only after the last reward. */
internal class ProTrialAdsUseCase(
  private val rewardedTrialAds: RewardedTrialAds,
  private val proTrialUseCase: ProTrialUseCase,
  private val analyticsEventSender: AnalyticsEventSender,
) {
  private val _adsWatched = MutableStateFlow(0)
  val adsWatched: StateFlow<Int> = _adsWatched.asStateFlow()

  suspend fun run(activity: Activity): ProTrialAdsResult {
    _adsWatched.value = 0
    var result = ProTrialAdsResult.CANCELLED
    try {
      result = watchAds(activity)
      return result
    } finally {
      if (result != ProTrialAdsResult.SUCCESS) _adsWatched.value = 0
    }
  }

  private suspend fun watchAds(activity: Activity): ProTrialAdsResult {
    repeat(REQUIRED_ADS) {
      when (rewardedTrialAds.show(activity)) {
        RewardedAdResult.REWARDED -> _adsWatched.value += 1
        RewardedAdResult.UNAVAILABLE -> return ProTrialAdsResult.ADS_UNAVAILABLE
        RewardedAdResult.DISMISSED -> return ProTrialAdsResult.CANCELLED
      }
    }
    proTrialUseCase.activate()
    analyticsEventSender.send(ProTrialActivatedEvent)
    return ProTrialAdsResult.SUCCESS
  }

  companion object {
    const val REQUIRED_ADS = 3
  }
}

package com.elementary.tasks.module.platform

import android.app.Activity
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.ProTrialActivatedEvent
import com.github.naz013.analytics.ProTrialExtendedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class ProTrialAdsResult {
  SUCCESS,
  ADS_UNAVAILABLE,
  CANCELLED,
  LIMIT_REACHED,
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
    return runAds(activity) {
      proTrialUseCase.activate()
      analyticsEventSender.send(ProTrialActivatedEvent)
      ProTrialAdsResult.SUCCESS
    }
  }

  suspend fun runExtend(activity: Activity): ProTrialAdsResult {
    if (!proTrialUseCase.canExtend()) return ProTrialAdsResult.LIMIT_REACHED
    return runAds(activity) {
      if (proTrialUseCase.extend()) {
        analyticsEventSender.send(ProTrialExtendedEvent)
        ProTrialAdsResult.SUCCESS
      } else {
        ProTrialAdsResult.LIMIT_REACHED
      }
    }
  }

  private suspend fun runAds(
    activity: Activity,
    onRewarded: () -> ProTrialAdsResult
  ): ProTrialAdsResult {
    _adsWatched.value = 0
    var result = ProTrialAdsResult.CANCELLED
    try {
      result = watchAds(activity, onRewarded)
      return result
    } finally {
      if (result != ProTrialAdsResult.SUCCESS) _adsWatched.value = 0
    }
  }

  private suspend fun watchAds(
    activity: Activity,
    onRewarded: () -> ProTrialAdsResult
  ): ProTrialAdsResult {
    repeat(REQUIRED_ADS) {
      when (rewardedTrialAds.show(activity)) {
        RewardedAdResult.REWARDED -> _adsWatched.value += 1
        RewardedAdResult.UNAVAILABLE -> return ProTrialAdsResult.ADS_UNAVAILABLE
        RewardedAdResult.DISMISSED -> return ProTrialAdsResult.CANCELLED
      }
    }
    return onRewarded()
  }

  companion object {
    const val REQUIRED_ADS = 1
  }
}

package com.elementary.tasks.module.platform

import android.app.Activity
import com.github.naz013.feature.common.coroutine.DispatcherProvider

/** The PRO build has no ads, so rewarded ads are never available. */
internal class RewardedTrialAdsImpl(
  @Suppress("unused") private val dispatcherProvider: DispatcherProvider,
) : RewardedTrialAds {
  override suspend fun show(activity: Activity): RewardedAdResult = RewardedAdResult.UNAVAILABLE
}

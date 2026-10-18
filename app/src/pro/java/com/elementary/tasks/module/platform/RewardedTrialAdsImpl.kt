package com.elementary.tasks.module.platform

import android.app.Activity

/** The PRO build has no ads, so rewarded ads are never available. */
internal class RewardedTrialAdsImpl : RewardedTrialAds {
  override suspend fun show(activity: Activity): RewardedAdResult = RewardedAdResult.UNAVAILABLE
}

package com.elementary.tasks.module.platform

import android.app.Activity

internal enum class RewardedAdResult {
  REWARDED,
  UNAVAILABLE,
  DISMISSED,
}

internal interface RewardedTrialAds {
  suspend fun show(activity: Activity): RewardedAdResult
}

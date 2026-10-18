package com.elementary.tasks.module.platform

import android.app.Activity
import com.elementary.tasks.AdsProvider
import com.github.naz013.logging.Logger
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class RewardedTrialAdsImpl : RewardedTrialAds {

  override suspend fun show(activity: Activity): RewardedAdResult {
    if (!AdsProvider.hasAds()) return RewardedAdResult.UNAVAILABLE
    val ad = load() ?: return RewardedAdResult.UNAVAILABLE
    return display(activity, ad)
  }

  private suspend fun load(): RewardedAd? = suspendCancellableCoroutine { continuation ->
    RewardedAd.load(
      AdRequest.Builder(AD_UNIT_ID).build(),
      object : AdLoadCallback<RewardedAd> {
        override fun onAdLoaded(ad: RewardedAd) {
          if (continuation.isActive) continuation.resume(ad)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Logger.e(TAG, "Rewarded ad failed to load: $adError")
          if (continuation.isActive) continuation.resume(null)
        }
      },
    )
  }

  private suspend fun display(activity: Activity, ad: RewardedAd): RewardedAdResult =
    suspendCancellableCoroutine { continuation ->
      var rewarded = false
      ad.adEventCallback = object : RewardedAdEventCallback {
        override fun onAdDismissedFullScreenContent() {
          val result = if (rewarded) RewardedAdResult.REWARDED else RewardedAdResult.DISMISSED
          if (continuation.isActive) continuation.resume(result)
        }

        override fun onAdFailedToShowFullScreenContent(
          fullScreenContentError: FullScreenContentError,
        ) {
          Logger.e(TAG, "Rewarded ad failed to show: $fullScreenContentError")
          if (continuation.isActive) continuation.resume(RewardedAdResult.UNAVAILABLE)
        }
      }
      ad.show(activity) { rewarded = true }
    }

  private companion object {
    private const val TAG = "RewardedTrialAds"

    // Placeholder (Google sample rewarded unit); replace with the real unit ID.
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
  }
}

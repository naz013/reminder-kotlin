package com.elementary.tasks

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.core.view.doOnDetach
import com.github.naz013.logging.Logger
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import java.lang.ref.WeakReference

class RotatingBannerAdsProvider(
  private val bannerId: String,
  viewGroup: ViewGroup,
  onAdsFailureCallback: OnAdsFailureCallback,
) {
  private val parent = WeakReference(viewGroup)
  private val callback = WeakReference(onAdsFailureCallback)

  private val handler = Handler(Looper.getMainLooper())
  private val runnable = Runnable { scheduleAds() }

  init {
    scheduleAds()
    listenParent()
  }

  private fun listenParent() {
    parent.get()?.doOnDetach {
      Logger.d(TAG, "Parent view is detached, will not show ADS")
      handler.removeCallbacks(runnable)
      callback.clear()
      parent.clear()
    }
  }

  private fun scheduleAds() {
    if (safeLoadAds()) {
      Logger.d(TAG, "Scheduled ads")
      handler.postDelayed(runnable, ADS_DURATION)
    }
  }

  private fun safeLoadAds(): Boolean = runCatching { loadAds() }.getOrNull() ?: false

  private fun loadAds(): Boolean {
    val viewGroup = parent.get() ?: return false.also {
      Logger.e(TAG, "Will not show ADS, Parent view is null")
    }
    val adView = AdView(viewGroup.context)

    viewGroup.removeAllViews()
    viewGroup.addView(adView)

    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(viewGroup.context, 360)
    val adRequest = BannerAdRequest.Builder(bannerId, adSize).build()
    adView.loadAd(
      adRequest,
      object : AdLoadCallback<BannerAd> {
        override fun onAdLoaded(ad: BannerAd) {
          adView.visible()
          ad.adEventCallback = object : BannerAdEventCallback {
              override fun onAdImpression() {
                Logger.d(TAG, "Banner ad recorded an impression.")
              }

              override fun onAdClicked() {
                Logger.d(TAG, "Banner ad clicked.")
              }
            }
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
          Logger.e(TAG, "Banner ad failed to load: $adError")
          adView.gone()
          callback.get()?.onAdsFailure()
        }
      },
    )
    return true
  }

  companion object {
    private const val TAG = "RotatingBannerAdsProvider"
    private const val ADS_DURATION = 1 * 60 * 1000L // 1 minute
  }
}

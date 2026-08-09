package com.elementary.tasks

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.doOnDetach
import com.elementary.tasks.ads.HouseAdBanner
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
  private var refreshCount = 0

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

  private fun safeLoadAds(): Boolean = runCatching {
    refreshCount++
    if (refreshCount % HOUSE_AD_EVERY_N_REFRESHES == 0) {
      loadHouseAd()
    } else {
      loadAds()
    }
  }.getOrNull() ?: false

  /** Shows a self-promoted "PRO" creative instead of a network ad, trading a slice of ad revenue
   *  on this one impression for a direct, zero-cash-cost upsell touchpoint. */
  private fun loadHouseAd(): Boolean {
    val viewGroup = parent.get() ?: return false.also {
      Logger.e(TAG, "Will not show house ad, Parent view is null")
    }
    val composeView = ComposeView(viewGroup.context).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
      setContent { HouseAdBanner() }
    }
    viewGroup.removeAllViews()
    viewGroup.addView(composeView)
    return true
  }

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

    /** Every 5th refresh (5 minutes, given [ADS_DURATION]) shows the house ad instead of loading
     *  a network ad. A plain constant is enough for a first test - no remote-config system. */
    private const val HOUSE_AD_EVERY_N_REFRESHES = 5
  }
}

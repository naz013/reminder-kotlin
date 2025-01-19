package com.elementary.tasks

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.core.view.doOnDetach
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import java.lang.ref.WeakReference

class RotatingBannerAdsProvider(
  private val bannerId: String,
  viewGroup: ViewGroup,
  onAdsFailureCallback: OnAdsFailureCallback
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

  private fun safeLoadAds(): Boolean {
    return runCatching { loadAds() }.getOrNull() ?: false
  }

  private fun loadAds(): Boolean {
    val viewGroup = parent.get() ?: return false.also {
      Logger.e(TAG, "Will not show ADS, Parent view is null")
    }
    val adView = AdView(viewGroup.context)
    adView.setAdSize(AdSize.LARGE_BANNER)
    adView.adUnitId = bannerId

    viewGroup.removeAllViews()
    viewGroup.addView(adView)

    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)

    adView.adListener = object : AdListener() {
      override fun onAdFailedToLoad(adError: LoadAdError) {
        adView.gone()
        callback.get()?.onAdsFailure()
      }

      override fun onAdLoaded() {
        adView.visible()
      }
    }
    return true
  }

  companion object {
    private const val TAG = "RotatingBannerAdsProvider"
    private const val ADS_DURATION = 1 * 60 * 1000L // 1 minute
  }
}

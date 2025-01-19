package com.elementary.tasks

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.doOnDetach
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.transparent
import com.github.naz013.ui.common.view.visible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import java.lang.ref.WeakReference

class RotatingNativeAdsProvider(
  private val bannerId: String,
  viewGroup: ViewGroup,
  @LayoutRes private val res: Int,
  onAdsFailureCallback: OnAdsFailureCallback
) {

  private val parent = WeakReference(viewGroup)
  private val callback = WeakReference(onAdsFailureCallback)
  private var nativeAdd: WeakReference<NativeAd>? = null

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

      nativeAdd?.get()?.destroy()
      callback.clear()
      parent.clear()
      nativeAdd = null
    }
  }

  private fun scheduleAds() {
    if (safeLoadAds()) {
      Logger.d(TAG, "Scheduled ads")
      handler.postDelayed(runnable, ADS_DURATION)
    } else {
      nativeAdd?.get()?.destroy()
      nativeAdd = null
    }
  }

  private fun safeLoadAds(): Boolean {
    return runCatching { loadAds() }.getOrNull() ?: false
  }

  private fun loadAds(): Boolean {
    val viewGroup = parent.get() ?: return false.also {
      Logger.e(TAG, "Will not show ADS, Parent view is null")
    }
    val adLoader = AdLoader.Builder(viewGroup.context, bannerId)
      .forNativeAd { ad: NativeAd ->
        nativeAdd?.get()?.destroy()
        nativeAdd = WeakReference(ad)
        val adView = LayoutInflater.from(viewGroup.context).inflate(res, null) as NativeAdView
        populateUnifiedNativeAdView(ad, adView)
        viewGroup.removeAllViews()
        viewGroup.addView(adView)
      }
      .withAdListener(object : AdListener() {
        override fun onAdFailedToLoad(error: LoadAdError) {
          super.onAdFailedToLoad(error)
          Logger.e("Failed to load native ad: ${error.message}")
          callback.get()?.onAdsFailure()
        }
      })
      .withNativeAdOptions(
        NativeAdOptions.Builder()
          .setRequestMultipleImages(false)
          .build()
      )
      .build()
    adLoader.loadAd(AdRequest.Builder().build())
    return true
  }

  private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    adView.mediaView = adView.findViewById(R.id.ad_media)

    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.priceView = adView.findViewById(R.id.ad_price)
    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    adView.storeView = adView.findViewById(R.id.ad_store)
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

    (adView.headlineView as TextView).text = nativeAd.headline
    if (nativeAd.body == null) {
      adView.bodyView?.transparent()
    } else {
      adView.bodyView?.visible()
      (adView.bodyView as TextView).text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
      adView.callToActionView?.transparent()
    } else {
      adView.callToActionView?.visible()
      (adView.callToActionView as Button).text = nativeAd.callToAction
    }

    if (nativeAd.icon == null) {
      adView.iconView?.gone()
    } else {
      (adView.iconView as ImageView).setImageDrawable(
        nativeAd.icon?.drawable
      )
      adView.iconView?.visible()
    }

    if (nativeAd.price == null) {
      adView.priceView?.transparent()
    } else {
      adView.priceView?.visible()
      (adView.priceView as TextView).text = nativeAd.price
    }

    if (nativeAd.store == null) {
      adView.storeView?.transparent()
    } else {
      adView.storeView?.visible()
      (adView.storeView as TextView).text = nativeAd.store
    }

    if (nativeAd.starRating == null) {
      adView.starRatingView?.transparent()
    } else {
      (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
      adView.starRatingView?.visible()
    }

    if (nativeAd.advertiser == null) {
      adView.advertiserView?.transparent()
    } else {
      (adView.advertiserView as TextView).text = nativeAd.advertiser
      adView.advertiserView?.visible()
    }
    adView.setNativeAd(nativeAd)
  }

  companion object {
    private const val TAG = "RotatingNativeAdsProvider"
    private const val ADS_DURATION = 1 * 60 * 1000L // 1 minute
  }
}

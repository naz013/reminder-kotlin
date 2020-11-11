package com.elementary.tasks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import timber.log.Timber

class AdsProvider {

  private var unifiedNativeAd: UnifiedNativeAd? = null

  init {
    wasError = false
  }

  fun showBanner(viewGroup: ViewGroup, bannerId: String, @LayoutRes res: Int, failListener: (() -> Unit)? = null) {
    val adLoader = AdLoader.Builder(viewGroup.context, bannerId)
      .forUnifiedNativeAd { ad: UnifiedNativeAd ->
        unifiedNativeAd?.destroy()
        unifiedNativeAd = ad
        val adView = LayoutInflater.from(viewGroup.context).inflate(res, null) as UnifiedNativeAdView
        populateUnifiedNativeAdView(ad, adView)
        viewGroup.removeAllViews()
        viewGroup.addView(adView)
      }
      .withAdListener(object : AdListener() {
          override fun onAdFailedToLoad(errorCode: Int) {
              Timber.d("onAdFailedToLoad: $errorCode")
              wasError = true
              failListener?.invoke()
          }
      })
      .withNativeAdOptions(NativeAdOptions.Builder()
        .setRequestMultipleImages(false)
        .build())
      .build()
    adLoader.loadAd(AdRequest.Builder().build())
  }

  fun destroy() {
    unifiedNativeAd?.destroy()
  }

  private fun populateUnifiedNativeAdView(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
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
      adView.bodyView.visibility = View.INVISIBLE
    } else {
      adView.bodyView.visibility = View.VISIBLE
      (adView.bodyView as TextView).text = nativeAd.body
    }

    if (nativeAd.callToAction == null) {
      adView.callToActionView.visibility = View.INVISIBLE
    } else {
      adView.callToActionView.visibility = View.VISIBLE
      (adView.callToActionView as Button).text = nativeAd.callToAction
    }

    if (nativeAd.icon == null) {
      adView.iconView.visibility = View.GONE
    } else {
      (adView.iconView as ImageView).setImageDrawable(
        nativeAd.icon.drawable)
      adView.iconView.visibility = View.VISIBLE
    }

    if (nativeAd.price == null) {
      adView.priceView.visibility = View.INVISIBLE
    } else {
      adView.priceView.visibility = View.VISIBLE
      (adView.priceView as TextView).text = nativeAd.price
    }

    if (nativeAd.store == null) {
      adView.storeView.visibility = View.INVISIBLE
    } else {
      adView.storeView.visibility = View.VISIBLE
      (adView.storeView as TextView).text = nativeAd.store
    }

    if (nativeAd.starRating == null) {
      adView.starRatingView.visibility = View.INVISIBLE
    } else {
      (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
      adView.starRatingView.visibility = View.VISIBLE
    }

    if (nativeAd.advertiser == null) {
      adView.advertiserView.visibility = View.INVISIBLE
    } else {
      (adView.advertiserView as TextView).text = nativeAd.advertiser
      adView.advertiserView.visibility = View.VISIBLE
    }
    adView.setNativeAd(nativeAd)
  }

  companion object {
    private const val ADMOB_ID = "ca-app-pub-5133908997831400~9675541050"
    const val REMINDER_BANNER_ID = "ca-app-pub-5133908997831400/1092844800"
    const val NOTE_BANNER_ID = "ca-app-pub-5133908997831400/9698572063"
    const val BIRTHDAY_BANNER_ID = "ca-app-pub-5133908997831400/5543225709"
    const val GTASKS_BANNER_ID = "ca-app-pub-5133908997831400/1152801971"
    const val REMINDER_PREVIEW_BANNER_ID = "ca-app-pub-5133908997831400/1084030852"
    const val NOTE_PREVIEW_BANNER_ID = "ca-app-pub-5133908997831400/4831704177"
    const val ADS_VIEW_TYPE = 100

    private var wasError = false

    fun numberOfAds(contentSize: Int): Int {
      if (contentSize == 0) return 0
      return when {
        contentSize >= 8 -> 2
        contentSize >= 16 -> 3
        else -> 1
      }
    }

    fun hasAds(): Boolean {
      return !wasError
    }

    fun init(context: Context) {
      MobileAds.initialize(context) {
        Timber.d("init: $it")
      }
    }
  }
}
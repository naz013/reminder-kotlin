package com.elementary.tasks

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.utils.visible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import timber.log.Timber

class AdsProvider {

  private var nativeAdd: NativeAd? = null
  private var consentInformation: ConsentInformation? = null
  private var consentForm: ConsentForm? = null

  init {
    wasError = false
  }

  fun showConsentMessage(activity: Activity) {
    val params = ConsentRequestParameters.Builder()
      .setTagForUnderAgeOfConsent(false)
      .build()

    UserMessagingPlatform.getConsentInformation(activity).also {
      consentInformation = it
    }.let {
      it.requestConsentInfoUpdate(
        activity,
        params,
        {
          if (it.isConsentFormAvailable) {
            loadForm(activity)
          }
        },
        { formError ->
          // Handle the error.
        }
      )
    }

  }

  private fun loadForm(activity: Activity) {
    UserMessagingPlatform.loadConsentForm(
      activity,
      { consentForm ->
        this.consentForm = consentForm
        if (consentInformation?.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
          consentForm.show(activity) { formError ->
            loadForm(activity)
          }
        }
      },
      { formError ->

      }
    )
  }

  fun showBanner(
    viewGroup: ViewGroup,
    bannerId: String,
    failListener: (() -> Unit)? = null
  ) {
    val adView = AdView(viewGroup.context)
    adView.setAdSize(AdSize.LARGE_BANNER)
    adView.adUnitId = bannerId

    viewGroup.removeAllViews()
    viewGroup.addView(adView)

    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)

    adView.adListener = object: AdListener() {
      override fun onAdFailedToLoad(adError : LoadAdError) {
        adView.gone()
        failListener?.invoke()
      }

      override fun onAdLoaded() {
        adView.visible()
      }
    }
  }

  fun showNativeBanner(
    viewGroup: ViewGroup,
    bannerId: String,
    @LayoutRes res: Int,
    failListener: (() -> Unit)? = null
  ) {
    val adLoader = AdLoader.Builder(viewGroup.context, bannerId)
      .forNativeAd { ad: NativeAd ->
        nativeAdd?.destroy()
        nativeAdd = ad
        val adView = LayoutInflater.from(viewGroup.context).inflate(res, null) as NativeAdView
        populateUnifiedNativeAdView(ad, adView)
        viewGroup.removeAllViews()
        viewGroup.addView(adView)
      }
      .withAdListener(object : AdListener() {
        override fun onAdFailedToLoad(error: LoadAdError) {
          super.onAdFailedToLoad(error)
          Timber.d("onAdFailedToLoad: $error")
          wasError = true
          failListener?.invoke()
        }
      })
      .withNativeAdOptions(
        NativeAdOptions.Builder()
          .setRequestMultipleImages(false)
          .build()
      )
      .build()
    adLoader.loadAd(AdRequest.Builder().build())
  }

  fun destroy() {
    nativeAdd?.destroy()
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
    private const val ADMOB_ID = "ca-app-pub-5133908997831400~9675541050"
    const val REMINDER_PREVIEW_BANNER_ID = "ca-app-pub-5133908997831400/1084030852"
    const val NOTE_PREVIEW_BANNER_ID = "ca-app-pub-5133908997831400/4831704177"
    const val BIRTHDAY_PREVIEW_BANNER_ID = "ca-app-pub-5133908997831400/1262280397"
    const val GOOGLE_TASKS_PREVIEW_BANNER_ID = "ca-app-pub-5133908997831400/5192898494"

    private var wasError = false

    fun hasAds(): Boolean {
      return !wasError
    }

    fun init(context: Context) {
      if (SuperUtil.isGooglePlayServicesAvailable(context)) {
        MobileAds.initialize(context) {
          Timber.d("init: $it")
        }
      } else {
        wasError = true
      }
    }
  }
}

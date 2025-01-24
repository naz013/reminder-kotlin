package com.elementary.tasks

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.logging.Logger
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class AdsProvider {

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
    RotatingBannerAdsProvider(
      bannerId = bannerId,
      viewGroup = viewGroup,
      onAdsFailureCallback = object : OnAdsFailureCallback {
        override fun onAdsFailure() {
          failListener?.invoke()
        }
      }
    )
  }

  fun showNativeBanner(
    viewGroup: ViewGroup,
    bannerId: String,
    @LayoutRes res: Int,
    failListener: (() -> Unit)? = null
  ) {
    RotatingNativeAdsProvider(
      viewGroup = viewGroup,
      bannerId = bannerId,
      res = res,
      onAdsFailureCallback = object : OnAdsFailureCallback {
        override fun onAdsFailure() {
          wasError = true
          failListener?.invoke()
        }
      }
    )
  }

  companion object {
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
          Logger.i("Ads provider initialized")
        }
      } else {
        wasError = true
      }
    }
  }
}

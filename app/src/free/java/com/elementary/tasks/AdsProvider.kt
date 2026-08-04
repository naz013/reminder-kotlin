package com.elementary.tasks

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.elementary.tasks.ads.AdBanner
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.logging.Logger
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
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
    val params = ConsentRequestParameters
        .Builder()
        .setTagForUnderAgeOfConsent(false)
        .build()

    UserMessagingPlatform
      .getConsentInformation(activity)
      .also {
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
          },
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
      },
    )
  }

  fun showBanner(
    viewGroup: ViewGroup,
    adBanner: AdBanner,
    failListener: (() -> Unit)? = null,
  ) {
    RotatingBannerAdsProvider(
      bannerId = adBanner.bannerId,
      viewGroup = viewGroup,
      onAdsFailureCallback =
        object : OnAdsFailureCallback {
          override fun onAdsFailure() {
            failListener?.invoke()
          }
        },
    )
  }

  private val AdBanner.bannerId: String
    get() {
      return when (this) {
        AdBanner.ReminderPreview -> "ca-app-pub-5133908997831400/5532170457"
        AdBanner.Birthday -> "ca-app-pub-5133908997831400/1262280397"
        AdBanner.NotePreview -> "ca-app-pub-5133908997831400/9399263275"
        AdBanner.GoogleTask -> "ca-app-pub-5133908997831400/5192898494"
        AdBanner.Group -> "ca-app-pub-5133908997831400/5460018266"
        AdBanner.GoogleTaskList -> "ca-app-pub-5133908997831400/6866147439"
        AdBanner.Tag -> "ca-app-pub-5133908997831400/1613820753"
        AdBanner.Place -> "ca-app-pub-5133908997831400/1110689651"
        AdBanner.PinLogin -> "ca-app-pub-5133908997831400/3859996131"
        AdBanner.ActionScreen -> "ca-app-pub-5133908997831400/8797607986"
      }
    }

  companion object {
    private const val TAG = "AdsProvider"

    private var wasError = false

    fun hasAds(): Boolean = !wasError

    fun init(context: Context, systemInfo: SystemInfo) {
      if (systemInfo.googlePlayServicesAvailable) {
        val initConfig = InitializationConfig.Builder("ca-app-pub-5133908997831400~9675541050").build()
        MobileAds.initialize(context, initConfig) {
          Logger.i(TAG, "Ads provider initialized")
        }
      } else {
        wasError = true
      }
    }
  }
}

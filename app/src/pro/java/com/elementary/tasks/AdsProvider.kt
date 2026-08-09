package com.elementary.tasks

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.elementary.tasks.ads.AdBanner
import com.github.naz013.platform.SystemInfo

class AdsProvider {
  fun showConsentMessage(activity: Activity) {}

  fun showBanner(
    viewGroup: ViewGroup,
    adBanner: AdBanner,
    failListener: (() -> Unit)? = null,
  ) {}

  companion object {
    fun hasAds() = false
    fun init(context: Context, systemInfo: SystemInfo) { }
  }
}

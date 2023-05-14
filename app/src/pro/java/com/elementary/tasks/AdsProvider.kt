package com.elementary.tasks

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes

class AdsProvider {

  fun showBanner(
    viewGroup: ViewGroup,
    bannerId: String,
    failListener: (() -> Unit)? = null
  ) {}

  fun showNativeBanner(
    viewGroup: ViewGroup,
    bannerId: String,
    @LayoutRes res: Int,
    failListener: (() -> Unit)? = null
  ) {}

  fun destroy() {
  }

  companion object {
    const val REMINDER_PREVIEW_BANNER_ID = ""
    const val NOTE_PREVIEW_BANNER_ID = ""
    const val BIRTHDAY_PREVIEW_BANNER_ID = ""
    const val GOOGLE_TASKS_PREVIEW_BANNER_ID = ""

    fun hasAds() = false

    fun init(context: Context) {
    }
  }
}

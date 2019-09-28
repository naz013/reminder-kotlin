package com.elementary.tasks

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes

class AdsProvider {

    fun showReminderBanner(viewGroup: ViewGroup, bannerId: String, @LayoutRes res: Int, failListener: (() -> Unit)? = null) {
    }

    fun destroy() {
    }

    companion object {
        const val REMINDER_BANNER_ID = ""
        const val ADS_VIEW_TYPE = 100

        fun numberOfAds(contentSize: Int): Int {
            return 0
        }

        fun hasAds() = false

        fun init(context: Context) {
        }
    }
}
package com.elementary.tasks.google_tasks.list

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.databinding.ListItemGoogleTaskAdsBinding

class GoogleTaskAdsHolder(
  parent: ViewGroup,
  adsProvider: AdsProvider,
  prefs: Prefs,
  failListener: () -> Unit
) : BaseHolder<ListItemGoogleTaskAdsBinding>(parent, R.layout.list_item_google_task_ads, prefs) {

  init {
    adsProvider.showBanner(
      binding.adsHolder,
      AdsProvider.GTASKS_BANNER_ID,
      R.layout.list_item_ads_hor,
      failListener
    )
  }

  companion object {

    fun updateList(list: List<GoogleTask>): List<GoogleTask> {
      return if (AdsProvider.hasAds() && list.isNotEmpty()) {
        val mutable = list.toMutableList()
        if (list.size > 6) {
          mutable.add(3, GoogleTask().apply { this.uuId = AdsProvider.GTASKS_BANNER_ID })
        } else {
          mutable.add(list.size / 2 + 1, GoogleTask().apply { this.uuId = AdsProvider.GTASKS_BANNER_ID })
        }
        mutable
      } else {
        list
      }
    }
  }
}

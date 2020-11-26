package com.elementary.tasks.google_tasks.list

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemGoogleTaskAdsBinding

class GoogleTaskAdsViewHolder(
  parent: ViewGroup,
  adsProvider: AdsProvider,
  currentStateHolder: CurrentStateHolder,
  failListener: () -> Unit
) : BaseViewHolder<ListItemGoogleTaskAdsBinding>(
  ListItemGoogleTaskAdsBinding.inflate(parent.inflater(), parent, false),
  currentStateHolder
) {

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

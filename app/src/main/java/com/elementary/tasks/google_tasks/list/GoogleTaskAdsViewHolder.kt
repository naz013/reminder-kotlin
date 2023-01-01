package com.elementary.tasks.google_tasks.list

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemGoogleTaskAdsBinding

class GoogleTaskAdsViewHolder(
  parent: ViewGroup,
  adsProvider: AdsProvider,
  failListener: () -> Unit
) : HolderBinding<ListItemGoogleTaskAdsBinding>(
  ListItemGoogleTaskAdsBinding.inflate(parent.inflater(), parent, false)
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

    fun updateList(list: List<UiGoogleTaskList>): List<UiGoogleTaskList> {
      return if (AdsProvider.hasAds() && list.isNotEmpty()) {
        val mutable = list.toMutableList()
        val adsItem = UiGoogleTaskList(
          text = "",
          notes = null,
          dueDate = null,
          statusIcon = null,
          id = AdsProvider.GTASKS_BANNER_ID
        )
        if (list.size > 6) {
          mutable.add(3, adsItem)
        } else {
          mutable.add(list.size / 2 + 1, adsItem)
        }
        mutable
      } else {
        list
      }
    }
  }
}

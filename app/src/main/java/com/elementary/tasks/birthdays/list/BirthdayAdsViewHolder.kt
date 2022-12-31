package com.elementary.tasks.birthdays.list

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.UiBirthdayList
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemBirthdayAdsBinding

class BirthdayAdsViewHolder(
  parent: ViewGroup,
  adsProvider: AdsProvider,
  failListener: () -> Unit
) : HolderBinding<ListItemBirthdayAdsBinding>(
  ListItemBirthdayAdsBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    adsProvider.showBanner(
      binding.adsHolder,
      AdsProvider.BIRTHDAY_BANNER_ID,
      R.layout.list_item_ads_hor,
      failListener
    )
  }

  companion object {

    fun updateList(list: List<UiBirthdayList>): List<UiBirthdayList> {
      return if (AdsProvider.hasAds() && list.isNotEmpty()) {
        val mutable = list.toMutableList()
        if (list.size > 6) {
          mutable.add(3, adsItem())
        } else {
          mutable.add(list.size / 2 + 1, adsItem())
        }
        mutable
      } else {
        list
      }
    }

    private fun adsItem() = UiBirthdayList(AdsProvider.BIRTHDAY_BANNER_ID)
  }
}

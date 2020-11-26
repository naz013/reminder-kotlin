package com.elementary.tasks.birthdays.list

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemBirthdayAdsBinding

class BirthdayAdsViewHolder(
  parent: ViewGroup,
  currentStateHolder: CurrentStateHolder,
  adsProvider: AdsProvider,
  failListener: () -> Unit
) : BaseViewHolder<ListItemBirthdayAdsBinding>(
  ListItemBirthdayAdsBinding.inflate(parent.inflater(), parent, false),
  currentStateHolder
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

    fun updateList(list: List<Birthday>): List<Birthday> {
      return if (AdsProvider.hasAds() && list.isNotEmpty()) {
        val mutable = list.toMutableList()
        if (list.size > 6) {
          mutable.add(3, Birthday().apply {
            this.uuId = AdsProvider.BIRTHDAY_BANNER_ID
          })
        } else {
          mutable.add(list.size / 2 + 1, Birthday().apply {
            this.uuId = AdsProvider.BIRTHDAY_BANNER_ID
          })
        }
        mutable
      } else {
        list
      }
    }
  }
}

package com.elementary.tasks.birthdays.list.filters

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.databinding.ListItemBirthdayAdsBinding

class BirthdayAdsHolder(parent: ViewGroup, adsProvider: AdsProvider, failListener: () -> Unit)
    : BaseHolder<ListItemBirthdayAdsBinding>(parent, R.layout.list_item_birthday_ads) {

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
                mutable.add(list.size / 2 + 1, Birthday().apply { this.uuId = AdsProvider.BIRTHDAY_BANNER_ID })
                mutable
            } else {
                list
            }
        }
    }
}

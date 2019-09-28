package com.elementary.tasks.reminder.lists.adapter

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.databinding.ListItemReminderAdsBinding

class ReminderAdsHolder(parent: ViewGroup, adsProvider: AdsProvider, failListener: () -> Unit)
    : BaseHolder<ListItemReminderAdsBinding>(parent, R.layout.list_item_reminder_ads) {

    init {
        adsProvider.showBanner(
                binding.adsHolder,
                AdsProvider.REMINDER_BANNER_ID,
                R.layout.list_item_ads_hor,
                failListener
        )
    }

    companion object {

        fun updateList(list: List<Reminder>): List<Reminder> {
            return if (AdsProvider.hasAds() && list.isNotEmpty()) {
                val mutable = list.toMutableList()
                mutable.add(list.size / 2 + 1, Reminder().apply { this.uuId = AdsProvider.REMINDER_BANNER_ID })
                mutable
            } else {
                list
            }
        }
    }
}

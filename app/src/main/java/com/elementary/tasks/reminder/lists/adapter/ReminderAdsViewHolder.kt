package com.elementary.tasks.reminder.lists.adapter

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemReminderAdsBinding

class ReminderAdsViewHolder(
  parent: ViewGroup,
  adsProvider: AdsProvider,
  currentStateHolder: CurrentStateHolder,
  failListener: () -> Unit
) : BaseViewHolder<ListItemReminderAdsBinding>(
  ListItemReminderAdsBinding.inflate(parent.inflater(), parent, false),
  currentStateHolder
) {

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
        if (list.size > 6) {
          mutable.add(3, Reminder().apply { this.uuId = AdsProvider.REMINDER_BANNER_ID })
        } else {
          mutable.add(list.size / 2 + 1, Reminder().apply { this.uuId = AdsProvider.REMINDER_BANNER_ID })
        }
        mutable
      } else {
        list
      }
    }
  }
}

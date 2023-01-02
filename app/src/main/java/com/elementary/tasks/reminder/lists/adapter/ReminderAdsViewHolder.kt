package com.elementary.tasks.reminder.lists.adapter

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListAds
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemReminderAdsBinding

class ReminderAdsViewHolder(
  parent: ViewGroup,
  adsProvider: AdsProvider,
  failListener: () -> Unit
) : BaseUiReminderListViewHolder<ListItemReminderAdsBinding, UiReminderListAds>(
  ListItemReminderAdsBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    adsProvider.showBanner(
      binding.adsHolder,
      AdsProvider.REMINDER_BANNER_ID,
      R.layout.list_item_ads_hor,
      failListener
    )
  }

  override fun setData(reminder: UiReminderListAds) {
  }

  companion object {

    fun addAdsIfNeeded(list: List<UiReminderList>): List<UiReminderList> {
      return if (AdsProvider.hasAds() && list.isNotEmpty()) {
        val mutable = list.toMutableList()
        if (list.size > 6) {
          mutable.add(3, UiReminderListAds())
        } else {
          mutable.add(list.size / 2 + 1, UiReminderListAds())
        }
        mutable
      } else {
        list
      }
    }
  }
}

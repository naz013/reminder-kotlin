package com.elementary.tasks.reminder.preview.adapter

import android.view.ViewGroup
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.BuildParams
import com.github.naz013.ui.common.view.inflater
import com.elementary.tasks.databinding.ListItemReminderPreviewAdsBinding

class ReminderAdsViewHolder(
  parent: ViewGroup,
  binding: ListItemReminderPreviewAdsBinding =
    ListItemReminderPreviewAdsBinding.inflate(parent.inflater(), parent, false)
) : HolderBinding<ListItemReminderPreviewAdsBinding>(binding) {

  private val adsProvider = AdsProvider()

  init {
    if (!BuildParams.isPro && AdsProvider.hasAds()) {
      adsProvider.showNativeBanner(
        binding.adsHolder,
        AdsProvider.REMINDER_PREVIEW_BANNER_ID,
        R.layout.list_item_ads_hor
      ) { }
    }
  }
}

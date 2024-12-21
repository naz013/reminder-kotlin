package com.elementary.tasks.reminder.lists.viewholder

import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.text.applyStyles
import com.github.naz013.feature.common.android.inflater
import com.elementary.tasks.databinding.ListItemReminderHeaderBinding
import com.elementary.tasks.reminder.lists.data.UiReminderListHeader

class HeaderViewHolder(
  parent: ViewGroup
) : HolderBinding<ListItemReminderHeaderBinding>(
  ListItemReminderHeaderBinding.inflate(parent.inflater(), parent, false)
) {

  fun bind(data: UiReminderListHeader) {
    binding.dateView.text = data.mainText.text
    binding.dateView.applyStyles(data.mainText.textFormat)
  }
}

package com.elementary.tasks.reminder.preview.adapter

import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.text.applyStyles
import com.github.naz013.feature.common.android.inflater
import com.elementary.tasks.databinding.ListItemReminderPreviewHeaderBinding
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewHeader

class ReminderHeaderViewHolder(
  parent: ViewGroup,
  binding: ListItemReminderPreviewHeaderBinding =
    ListItemReminderPreviewHeaderBinding.inflate(parent.inflater(), parent, false)
) : HolderBinding<ListItemReminderPreviewHeaderBinding>(binding) {

  fun bind(header: UiReminderPreviewHeader) {
    binding.headerTitleView.text = header.textElement.text
    binding.headerTitleView.applyStyles(header.textElement.textFormat)
  }
}

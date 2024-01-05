package com.elementary.tasks.reminder.preview.adapter

import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.text.applyStyles
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.databinding.ListItemReminderPreviewStatusBinding
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewStatus

class ReminderStatusViewHolder(
  parent: ViewGroup,
  onToggleClicked: (Int) -> Unit,
  binding: ListItemReminderPreviewStatusBinding =
    ListItemReminderPreviewStatusBinding.inflate(parent.inflater(), parent, false)
) : HolderBinding<ListItemReminderPreviewStatusBinding>(binding) {

  init {
    binding.statusSwitchView.isFocusableInTouchMode = false
    binding.root.setOnClickListener {
      onToggleClicked(bindingAdapterPosition)
    }
  }

  fun bind(status: UiReminderPreviewStatus) {
    binding.statusTextView.text = status.statusText.text
    binding.statusTextView.applyStyles(status.statusText.textFormat)

    binding.statusSwitchView.isChecked = status.status.active
  }
}

package com.elementary.tasks.reminder.preview.adapter

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.text.applyStyles
import com.github.naz013.ui.common.view.inflater
import com.github.naz013.ui.common.view.visibleInvisible
import com.elementary.tasks.databinding.ListItemReminderPreviewElementBinding
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewElement

class ReminderElementViewHolder(
  parent: ViewGroup,
  binding: ListItemReminderPreviewElementBinding =
    ListItemReminderPreviewElementBinding.inflate(parent.inflater(), parent, false)
) : HolderBinding<ListItemReminderPreviewElementBinding>(binding) {

  fun bind(element: UiReminderPreviewElement) {
    binding.elementTextView.text = element.textElement.text
    binding.elementTextView.applyStyles(element.textElement.textFormat)

    binding.elementIconImageView.visibleInvisible(element.icon != null)
    element.icon?.run {
      binding.elementIconImageView.setImageResource(value)
      binding.elementIconImageView.imageTintList = ColorStateList.valueOf(color)
    }
  }
}

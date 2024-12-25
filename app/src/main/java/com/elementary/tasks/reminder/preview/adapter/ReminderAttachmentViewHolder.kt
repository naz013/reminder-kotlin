package com.elementary.tasks.reminder.preview.adapter

import android.view.ViewGroup
import coil.load
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.text.applyStyles
import com.github.naz013.ui.common.view.inflater
import com.elementary.tasks.databinding.ListItemReminderPreviewAttachmentBinding
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.AttachmentType
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewAttachment

class ReminderAttachmentViewHolder(
  parent: ViewGroup,
  binding: ListItemReminderPreviewAttachmentBinding =
    ListItemReminderPreviewAttachmentBinding.inflate(parent.inflater(), parent, false)
) : HolderBinding<ListItemReminderPreviewAttachmentBinding>(binding) {

  fun bind(element: UiReminderPreviewAttachment) {
    binding.elementTextView.text = element.text.text
    binding.elementTextView.applyStyles(element.text.textFormat)
    binding.elementIconImageView.setImageResource(element.file.icon)
    if (element.file.type == AttachmentType.IMAGE) {
      binding.elementIconImageView.load(element.file.uri)
    }
  }
}

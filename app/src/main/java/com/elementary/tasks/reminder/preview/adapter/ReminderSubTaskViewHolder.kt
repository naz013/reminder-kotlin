package com.elementary.tasks.reminder.preview.adapter

import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.text.applyStyles
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.databinding.ListItemReminderPreviewSubTaskBinding
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewSubTask

class ReminderSubTaskViewHolder(
  parent: ViewGroup,
  removeClick: (Int) -> Unit,
  checkClick: (Int) -> Unit,
  binding: ListItemReminderPreviewSubTaskBinding =
    ListItemReminderPreviewSubTaskBinding.inflate(parent.inflater(), parent, false)
) : HolderBinding<ListItemReminderPreviewSubTaskBinding>(binding) {

  init {
    binding.clearButton.setOnClickListener { removeClick(bindingAdapterPosition) }
    binding.checkView.setOnClickListener { checkClick(bindingAdapterPosition) }
  }

  fun bind(subTask: UiReminderPreviewSubTask) {
    binding.textView.text = subTask.textElement.text
    binding.textView.applyStyles(subTask.textElement.textFormat)

    binding.checkView.setImageResource(
      if (subTask.isChecked) {
        R.drawable.ic_fluent_checkbox_checked
      } else {
        R.drawable.ic_fluent_checkbox_unchecked
      }
    )
  }
}

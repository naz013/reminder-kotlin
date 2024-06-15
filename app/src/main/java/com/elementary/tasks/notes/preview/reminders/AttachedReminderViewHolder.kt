package com.elementary.tasks.notes.preview.reminders

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.core.utils.ui.setTextOrHide
import com.elementary.tasks.databinding.ListItemNoteAttachedReminderBinding

class AttachedReminderViewHolder(
  parent: ViewGroup,
  private val onEditClicked: (Int) -> Unit,
  private val onDetachClicked: (Int) -> Unit,
  private val binding: ListItemNoteAttachedReminderBinding =
    ListItemNoteAttachedReminderBinding.inflate(
      parent.inflater(),
      parent,
      false
    )
) : RecyclerView.ViewHolder(binding.root) {

  init {
    binding.editReminder.setOnClickListener { onEditClicked(bindingAdapterPosition) }
    binding.detachReminder.setOnClickListener { onDetachClicked(bindingAdapterPosition) }
  }

  fun bind(item: UiNoteAttachedReminder) {
    binding.reminderSummary.setTextOrHide(item.summary)
    binding.reminderTime.setTextOrHide(item.dateTime)
  }
}

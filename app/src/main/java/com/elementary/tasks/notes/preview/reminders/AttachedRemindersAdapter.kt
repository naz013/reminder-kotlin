package com.elementary.tasks.notes.preview.reminders

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class AttachedRemindersAdapter(
  private val onEdit: (UiNoteAttachedReminder) -> Unit,
  private val onDetach: (UiNoteAttachedReminder) -> Unit
) : ListAdapter<UiNoteAttachedReminder, AttachedReminderViewHolder>(
  DiffCallback()
) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachedReminderViewHolder {
    return AttachedReminderViewHolder(
      parent = parent,
      onEditClicked = { onEdit(getItem(it)) },
      onDetachClicked = { onDetach(getItem(it)) }
    )
  }

  override fun onBindViewHolder(holder: AttachedReminderViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  private class DiffCallback : DiffUtil.ItemCallback<UiNoteAttachedReminder>() {
    override fun areItemsTheSame(
      oldItem: UiNoteAttachedReminder,
      newItem: UiNoteAttachedReminder
    ): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
      oldItem: UiNoteAttachedReminder,
      newItem: UiNoteAttachedReminder
    ): Boolean {
      return oldItem == newItem
    }
  }
}

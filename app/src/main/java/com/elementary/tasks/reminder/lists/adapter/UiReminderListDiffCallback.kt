package com.elementary.tasks.reminder.lists.adapter

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.UiReminderList

class UiReminderListDiffCallback : DiffUtil.ItemCallback<UiReminderList>() {

  override fun areContentsTheSame(oldItem: UiReminderList, newItem: UiReminderList): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: UiReminderList, newItem: UiReminderList): Boolean {
    return oldItem.id == newItem.id
  }
}
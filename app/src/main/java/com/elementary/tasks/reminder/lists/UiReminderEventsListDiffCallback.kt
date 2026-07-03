package com.elementary.tasks.reminder.lists

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList

class UiReminderEventsListDiffCallback : DiffUtil.ItemCallback<UiReminderEventsList>() {
  override fun areContentsTheSame(
    oldItem: UiReminderEventsList,
    newItem: UiReminderEventsList,
  ): Boolean = oldItem == newItem

  override fun areItemsTheSame(
    oldItem: UiReminderEventsList,
    newItem: UiReminderEventsList,
  ): Boolean = oldItem.id == newItem.id
}

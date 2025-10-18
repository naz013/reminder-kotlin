package com.elementary.tasks.calendar.data

import androidx.recyclerview.widget.DiffUtil

object EventModelDiffCallback : DiffUtil.ItemCallback<EventModel>() {

  override fun areItemsTheSame(oldItem: EventModel, newItem: EventModel): Boolean {
    return when {
      oldItem is ReminderEventModel && newItem is ReminderEventModel ->
        oldItem.model.id == newItem.model.id
      oldItem is BirthdayEventModel && newItem is BirthdayEventModel ->
        oldItem.model.uuId == newItem.model.uuId
      // Add more cases here for other EventModel subtypes with unique IDs
      else -> false
    }
  }

  override fun areContentsTheSame(oldItem: EventModel, newItem: EventModel): Boolean {
    return oldItem == newItem
  }
}

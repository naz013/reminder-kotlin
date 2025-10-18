package com.elementary.tasks.calendar.data

import androidx.recyclerview.widget.DiffUtil

object EventModelDiffCallback : DiffUtil.ItemCallback<EventModel>() {

  override fun areItemsTheSame(oldItem: EventModel, newItem: EventModel): Boolean {
    return oldItem.viewType == newItem.viewType &&
      oldItem.day == newItem.day &&
      oldItem.monthValue == newItem.monthValue &&
      oldItem.year == newItem.year
  }

  override fun areContentsTheSame(oldItem: EventModel, newItem: EventModel): Boolean {
    return oldItem == newItem
  }
}

package com.elementary.tasks.home.scheduleview

import androidx.recyclerview.widget.DiffUtil

class ScheduleModelDiffCallback : DiffUtil.ItemCallback<ScheduleModel>() {

  override fun areItemsTheSame(oldItem: ScheduleModel, newItem: ScheduleModel): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(oldItem: ScheduleModel, newItem: ScheduleModel): Boolean {
    return oldItem == newItem
  }
}

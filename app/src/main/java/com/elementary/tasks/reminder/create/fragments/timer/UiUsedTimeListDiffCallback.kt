package com.elementary.tasks.reminder.create.fragments.timer

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.UiUsedTimeList

class UiUsedTimeListDiffCallback : DiffUtil.ItemCallback<UiUsedTimeList>() {

  override fun areContentsTheSame(oldItem: UiUsedTimeList, newItem: UiUsedTimeList): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: UiUsedTimeList, newItem: UiUsedTimeList): Boolean {
    return oldItem.timeString == newItem.timeString
  }
}

package com.elementary.tasks.groups.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.models.ReminderGroup

class GroupDiffCallback : DiffUtil.ItemCallback<ReminderGroup>() {

  override fun areContentsTheSame(oldItem: ReminderGroup, newItem: ReminderGroup): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: ReminderGroup, newItem: ReminderGroup): Boolean {
    return oldItem.groupUuId == newItem.groupUuId
  }
}

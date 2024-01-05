package com.elementary.tasks.reminder.build.adapter

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.reminder.build.UiBuilderItem

class UiBuilderItemDiffCallback : DiffUtil.ItemCallback<UiBuilderItem>() {
  override fun areItemsTheSame(oldItem: UiBuilderItem, newItem: UiBuilderItem): Boolean {
    return oldItem.key == newItem.key
  }

  override fun areContentsTheSame(oldItem: UiBuilderItem, newItem: UiBuilderItem): Boolean {
    return oldItem == newItem
  }
}

package com.elementary.tasks.reminder.build.selectordialog.params

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.reminder.build.UiSelectorItem

class UiSelectorItemDiffCallback : DiffUtil.ItemCallback<UiSelectorItem>() {
  override fun areItemsTheSame(oldItem: UiSelectorItem, newItem: UiSelectorItem): Boolean {
    return oldItem.builderItem.biType == newItem.builderItem.biType
  }

  override fun areContentsTheSame(oldItem: UiSelectorItem, newItem: UiSelectorItem): Boolean {
    return oldItem == newItem
  }
}

package com.elementary.tasks.reminder.create.fragments.recur.preview

import androidx.recyclerview.widget.DiffUtil
import java.util.Objects

class PreviewItemDiffCallback : DiffUtil.ItemCallback<PreviewItem>() {

  override fun areItemsTheSame(oldItem: PreviewItem, newItem: PreviewItem): Boolean {
    return oldItem == newItem
  }

  override fun areContentsTheSame(oldItem: PreviewItem, newItem: PreviewItem): Boolean {
    return Objects.equals(oldItem, newItem)
  }
}

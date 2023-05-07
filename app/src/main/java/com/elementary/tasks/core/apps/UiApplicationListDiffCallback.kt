package com.elementary.tasks.core.apps

import androidx.recyclerview.widget.DiffUtil

@Deprecated("After S")
class UiApplicationListDiffCallback : DiffUtil.ItemCallback<UiApplicationList>() {

  override fun areContentsTheSame(oldItem: UiApplicationList, newItem: UiApplicationList): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: UiApplicationList, newItem: UiApplicationList): Boolean {
    return oldItem.packageName == newItem.packageName
  }
}

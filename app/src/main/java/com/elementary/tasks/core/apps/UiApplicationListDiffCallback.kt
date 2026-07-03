package com.elementary.tasks.core.apps

import androidx.recyclerview.widget.DiffUtil

@Deprecated("After S")
class UiApplicationListDiffCallback : DiffUtil.ItemCallback<UiApplicationList>() {
  override fun areContentsTheSame(
    oldItem: UiApplicationList,
    newItem: UiApplicationList,
  ): Boolean = oldItem == newItem

  override fun areItemsTheSame(
    oldItem: UiApplicationList,
    newItem: UiApplicationList,
  ): Boolean = oldItem.packageName == newItem.packageName
}

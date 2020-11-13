package com.elementary.tasks.core.apps

import androidx.recyclerview.widget.DiffUtil

class AppsDiffCallback : DiffUtil.ItemCallback<ApplicationItem>() {

  override fun areContentsTheSame(oldItem: ApplicationItem, newItem: ApplicationItem): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: ApplicationItem, newItem: ApplicationItem): Boolean {
    return oldItem.packageName == newItem.packageName
  }
}
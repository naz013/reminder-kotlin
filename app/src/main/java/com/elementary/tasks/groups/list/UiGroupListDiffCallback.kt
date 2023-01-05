package com.elementary.tasks.groups.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.group.UiGroupList

class UiGroupListDiffCallback : DiffUtil.ItemCallback<UiGroupList>() {

  override fun areContentsTheSame(oldItem: UiGroupList, newItem: UiGroupList): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: UiGroupList, newItem: UiGroupList): Boolean {
    return oldItem.id == newItem.id
  }
}

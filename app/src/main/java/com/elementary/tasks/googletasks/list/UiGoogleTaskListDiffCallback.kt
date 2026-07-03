package com.elementary.tasks.googletasks.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList

class UiGoogleTaskListDiffCallback : DiffUtil.ItemCallback<UiGoogleTaskList>() {
  override fun areContentsTheSame(
    oldItem: UiGoogleTaskList,
    newItem: UiGoogleTaskList,
  ): Boolean = oldItem == newItem

  override fun areItemsTheSame(
    oldItem: UiGoogleTaskList,
    newItem: UiGoogleTaskList,
  ): Boolean = oldItem.id == newItem.id
}

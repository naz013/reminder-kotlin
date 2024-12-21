package com.elementary.tasks.googletasks.list

import androidx.recyclerview.widget.DiffUtil
import com.github.naz013.domain.GoogleTaskList

class GoogleTasksListDiffCallback : DiffUtil.ItemCallback<GoogleTaskList>() {

  override fun areContentsTheSame(oldItem: GoogleTaskList, newItem: GoogleTaskList): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: GoogleTaskList, newItem: GoogleTaskList): Boolean {
    return oldItem.listId == newItem.listId
  }
}

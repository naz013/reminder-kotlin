package com.elementary.tasks.google_tasks.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.models.GoogleTaskList

class GoogleTasksListDiffCallback : DiffUtil.ItemCallback<GoogleTaskList>() {

  override fun areContentsTheSame(oldItem: GoogleTaskList, newItem: GoogleTaskList): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: GoogleTaskList, newItem: GoogleTaskList): Boolean {
    return oldItem.listId == newItem.listId
  }
}

package com.elementary.tasks.google_tasks.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.models.GoogleTask

class GoogleTaskDiffCallback : DiffUtil.ItemCallback<GoogleTask>() {

    override fun areContentsTheSame(oldItem: GoogleTask, newItem: GoogleTask): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: GoogleTask, newItem: GoogleTask): Boolean {
        return oldItem.taskId == newItem.taskId
    }
}
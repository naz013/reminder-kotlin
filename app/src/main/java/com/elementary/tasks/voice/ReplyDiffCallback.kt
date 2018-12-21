package com.elementary.tasks.voice

import androidx.recyclerview.widget.DiffUtil

class ReplyDiffCallback : DiffUtil.ItemCallback<Reply>() {
    override fun areItemsTheSame(oldItem: Reply, newItem: Reply): Boolean {
        return oldItem.viewType == newItem.viewType
    }

    override fun areContentsTheSame(oldItem: Reply, newItem: Reply): Boolean {
        return oldItem == newItem
    }
}
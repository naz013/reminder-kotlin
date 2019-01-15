package com.elementary.tasks.voice

import androidx.recyclerview.widget.DiffUtil
import timber.log.Timber

class ReplyDiffCallback : DiffUtil.ItemCallback<Reply>() {
    override fun areItemsTheSame(oldItem: Reply, newItem: Reply): Boolean {
        Timber.d("areItemsTheSame: $oldItem, $newItem")
        return oldItem.uuId == newItem.uuId
    }

    override fun areContentsTheSame(oldItem: Reply, newItem: Reply): Boolean {
        val res = if (oldItem.content is String) {
            oldItem.uuId == newItem.uuId && oldItem.content == newItem.content && oldItem.viewType == newItem.viewType
        } else {
            oldItem == newItem
        }
        Timber.d("areContentsTheSame: $res, $oldItem, $newItem")
        return res
    }
}
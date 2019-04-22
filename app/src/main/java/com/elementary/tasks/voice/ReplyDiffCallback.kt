package com.elementary.tasks.voice

import androidx.recyclerview.widget.DiffUtil
import timber.log.Timber

class ReplyDiffCallback(private val oldList: List<Reply>, private val newList: List<Reply>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uuId == newList[newItemPosition].uuId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    private fun areContentsTheSame(oldItem: Reply, newItem: Reply): Boolean {
        val res = if (oldItem.content is String) {
            oldItem.uuId == newItem.uuId && oldItem.content == newItem.content && oldItem.viewType == newItem.viewType
        } else {
            oldItem == newItem
        }
        Timber.d("areContentsTheSame: $res, $oldItem, $newItem")
        return res
    }
}
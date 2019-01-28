package com.elementary.tasks.notes.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.models.ImageFile

class ImageDIffCallback : DiffUtil.ItemCallback<ImageFile>() {

    override fun areContentsTheSame(oldItem: ImageFile, newItem: ImageFile): Boolean {
        return oldItem.id == newItem.id
                && oldItem.noteId == newItem.noteId
                && oldItem.state.id == newItem.state.id
                && oldItem.image?.contentEquals(newItem.image ?: ByteArray(0)) ?: false
    }

    override fun areItemsTheSame(oldItem: ImageFile, newItem: ImageFile): Boolean {
        return oldItem.id == newItem.id
    }
}
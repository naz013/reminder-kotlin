package com.elementary.tasks.notes.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.models.NoteWithImages

class NoteDIffCallback : DiffUtil.ItemCallback<NoteWithImages>() {

    override fun areContentsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
        return oldItem.getKey() == newItem.getKey()
    }
}
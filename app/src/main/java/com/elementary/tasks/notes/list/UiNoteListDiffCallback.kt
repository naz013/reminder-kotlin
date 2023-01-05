package com.elementary.tasks.notes.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.note.UiNoteList

class UiNoteListDiffCallback : DiffUtil.ItemCallback<UiNoteList>() {

  override fun areContentsTheSame(oldItem: UiNoteList, newItem: UiNoteList): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: UiNoteList, newItem: UiNoteList): Boolean {
    return oldItem.id == newItem.id
  }
}

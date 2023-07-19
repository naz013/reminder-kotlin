package com.elementary.tasks.core.app_widgets.singlenote

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.note.UiNoteListSelectable

class UiNoteListSelectableDiffCallback : DiffUtil.ItemCallback<UiNoteListSelectable>() {

  override fun areContentsTheSame(oldItem: UiNoteListSelectable, newItem: UiNoteListSelectable): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: UiNoteListSelectable, newItem: UiNoteListSelectable): Boolean {
    return oldItem.id == newItem.id
  }
}

package com.elementary.tasks.notes.create.images

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.note.UiNoteImage

class UiNoteImageDiffCallback : DiffUtil.ItemCallback<UiNoteImage>() {

  override fun areContentsTheSame(oldItem: UiNoteImage, newItem: UiNoteImage): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: UiNoteImage, newItem: UiNoteImage): Boolean {
    return oldItem.id == newItem.id
  }
}

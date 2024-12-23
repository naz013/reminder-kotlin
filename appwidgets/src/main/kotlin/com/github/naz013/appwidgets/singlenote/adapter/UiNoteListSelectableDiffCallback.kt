package com.github.naz013.appwidgets.singlenote.adapter

import androidx.recyclerview.widget.DiffUtil
import com.github.naz013.appwidgets.singlenote.data.UiNoteListSelectable

internal class UiNoteListSelectableDiffCallback : DiffUtil.ItemCallback<UiNoteListSelectable>() {

  override fun areContentsTheSame(
    oldItem: UiNoteListSelectable,
    newItem: UiNoteListSelectable
  ): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(
    oldItem: UiNoteListSelectable,
    newItem: UiNoteListSelectable
  ): Boolean {
    return oldItem.id == newItem.id
  }
}

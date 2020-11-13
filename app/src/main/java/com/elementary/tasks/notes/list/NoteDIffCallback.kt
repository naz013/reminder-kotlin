package com.elementary.tasks.notes.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.models.NoteWithImages

class NoteDIffCallback : DiffUtil.ItemCallback<NoteWithImages>() {

  override fun areContentsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
    return oldItem.getColor() == newItem.getColor()
      && oldItem.getGmtTime() == newItem.getGmtTime()
      && oldItem.getOpacity() == newItem.getOpacity()
      && oldItem.getSummary() == newItem.getSummary()
      && oldItem.getStyle() == newItem.getStyle()
      && oldItem.images == newItem.images
  }

  override fun areItemsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
    return oldItem.getKey() == newItem.getKey()
  }
}
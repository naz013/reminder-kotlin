package com.elementary.tasks.notes.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.interfaces.ActionsListener

class NotesRecyclerAdapter : ListAdapter<UiNoteList, NoteViewHolder>(UiNoteListDiffCallback()) {

  var actionsListener: ActionsListener<UiNoteList>? = null
  var imageClickListener: ((note: UiNoteList, imagePosition: Int) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
    return NoteViewHolder(
      parent,
      listener = { view, i, listActions ->
        actionsListener?.onAction(view, i, getItem(i), listActions)
      },
      imageClickListener = { _, position, imageId ->
        val item = getItem(position)
        val imagePosition = item.images.indexOfFirst { it.id == imageId }.takeIf { it != -1 } ?: 0
        imageClickListener?.invoke(item, imagePosition)
      }
    )
  }

  override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
    holder.setData(getItem(position))
  }
}

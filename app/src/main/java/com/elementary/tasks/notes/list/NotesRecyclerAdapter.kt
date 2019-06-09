package com.elementary.tasks.notes.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.interfaces.ActionsListener

class NotesRecyclerAdapter : ListAdapter<NoteWithImages, NoteHolder>(NoteDIffCallback()) {

    var actionsListener: ActionsListener<NoteWithImages>? = null

    override fun submitList(list: List<NoteWithImages>?) {
        super.submitList(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        return NoteHolder(parent) { view, i, listActions ->
            if (actionsListener != null) {
                actionsListener?.onAction(view, i, getItem(i), listActions)
            }
        }
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.setData(getItem(position))
    }
}

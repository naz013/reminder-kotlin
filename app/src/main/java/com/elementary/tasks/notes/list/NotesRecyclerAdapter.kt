package com.elementary.tasks.notes.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.interfaces.ActionsListener
import java.util.*

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class NotesRecyclerAdapter : RecyclerView.Adapter<NoteHolder>() {

    private val mData = ArrayList<Note>()
    var actionsListener: ActionsListener<Note>? = null

    var data: List<Note>
        get() = mData
        set(list) {
            this.mData.clear()
            this.mData.addAll(list)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun getItem(position: Int): Note {
        return mData[position]
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

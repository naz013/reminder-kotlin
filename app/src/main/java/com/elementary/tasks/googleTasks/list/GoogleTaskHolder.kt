package com.elementary.tasks.googleTasks.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.ListActions
import kotlinx.android.synthetic.main.list_item_task.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Copyright 2018 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class GoogleTaskHolder (parent: ViewGroup, val map: Map<String, GoogleTaskList>, listener: ((View, Int, ListActions) -> Unit)?) :
        BaseHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_task, parent, false)) {

    fun bind(googleTask: GoogleTask) {
        itemView.task.text = googleTask.title
        itemView.note.text = googleTask.notes
        if (googleTask.notes.isEmpty()) {
            itemView.note.visibility = View.GONE
        } else {
            itemView.note.visibility = View.VISIBLE
        }
        loadDue(itemView.taskDate, googleTask.dueDate)
        loadCheck(itemView.statusIcon, googleTask)
    }

    init {
        itemView.clickView.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.EDIT) }
        itemView.statusIcon.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.SWITCH) }
    }

    private fun loadCheck(view: ImageView, item: GoogleTask) {
        var color = themeUtil.getNoteLightColor(0)
        if (item.listId != "" && map.containsKey(item.listId)) {
            val googleTaskList = map[item.listId]
            if (googleTaskList != null) {
                color = themeUtil.getNoteLightColor(googleTaskList.color)
            }
        }
        if (item.status == GTasks.TASKS_COMPLETE) {
            view.setImageResource(R.drawable.ic_check)
        } else {
            view.setImageResource(R.drawable.ic_empty_circle)
        }
        DrawableCompat.setTint(view.drawable, color)
    }

    private fun loadDue(view: TextView, due: Long) {
        val full24Format = SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        if (due != 0L) {
            calendar.timeInMillis = due
            val update = full24Format.format(calendar.time)
            view.text = update
        } else {
            view.visibility = View.INVISIBLE
        }
    }
}
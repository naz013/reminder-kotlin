package com.elementary.tasks.google_tasks.list

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ListItemGoogleTaskBinding
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
class GoogleTaskHolder (parent: ViewGroup, listener: ((View, Int, ListActions) -> Unit)?) :
        BaseHolder<ListItemGoogleTaskBinding>(parent, R.layout.list_item_google_task) {

    init {
        binding.clickView.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.EDIT) }
        binding.statusIcon.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.SWITCH) }
    }

    fun bind(googleTask: GoogleTask, map: Map<String, GoogleTaskList>) {
        binding.task.text = googleTask.title
        binding.note.text = googleTask.notes
        if (googleTask.notes.isEmpty()) {
            binding.note.visibility = View.GONE
        } else {
            binding.note.visibility = View.VISIBLE
        }
        loadDue(binding.taskDate, googleTask.dueDate)
        loadCheck(binding.statusIcon, googleTask, map)
    }

    private fun loadCheck(view: ImageView, item: GoogleTask, map: Map<String, GoogleTaskList>) {
        val color = if (item.listId != "" && map.containsKey(item.listId)) {
            val googleTaskList = map[item.listId]
            if (googleTaskList != null) {
                themeUtil.getNoteLightColor(googleTaskList.color)
            } else {
                themeUtil.getNoteLightColor(0)
            }
        } else {
            themeUtil.getNoteLightColor(0)
        }
        view.setImageBitmap(createIcon(view.context, item.status == GTasks.TASKS_COMPLETE, color))
    }

    private fun createIcon(context: Context, isChecked: Boolean, color: Int): Bitmap? {
        return if (isChecked) {
            ViewUtils.createIcon(context, R.drawable.ic_check, color)
        } else {
            ViewUtils.createIcon(context, R.drawable.ic_empty_circle, color)
        }
    }

    private fun loadDue(view: TextView, due: Long) {
        val full24Format = SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        if (due != 0L) {
            calendar.timeInMillis = due
            val update = full24Format.format(calendar.time)
            view.text = update
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.INVISIBLE
        }
    }
}
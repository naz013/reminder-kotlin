package com.elementary.tasks.google_tasks.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.views.roboto.RoboCheckBox
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.list_item_task.view.*
import java.text.SimpleDateFormat
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
class TasksRecyclerAdapter internal constructor() : RecyclerView.Adapter<TasksRecyclerAdapter.ViewHolder>() {

    private var googleTasks: List<GoogleTask> = ArrayList()
    var actionsListener: ActionsListener<GoogleTask>? = null

    fun setGoogleTasks(googleTasks: List<GoogleTask>) {
        this.googleTasks = googleTasks
        notifyDataSetChanged()
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(googleTask: GoogleTask) {
            itemView.task.text = googleTask.title
            itemView.note.text = googleTask.notes
            loadTaskCard(itemView.card, googleTask.hidden)
            loadMarker(itemView.listColor, googleTask.listId)
            loadDue(itemView.taskDate, googleTask.dueDate)
            loadCheck(itemView.checkDone, googleTask)
        }

        init {
            itemView.onClick { onItemClick(it, adapterPosition) }
            itemView.checkDone.onClick { switchTask(it, adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_task, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(googleTasks[position])
    }

    override fun getItemCount(): Int {
        return googleTasks.size
    }

    private fun onItemClick(v: View, position: Int) {
        if (actionsListener != null) {
            actionsListener!!.onAction(v, position, googleTasks[position], ListActions.EDIT)
        }
    }

    private fun switchTask(v: View, position: Int) {
        if (actionsListener != null) {
            actionsListener!!.onAction(v, position, googleTasks[position], ListActions.SWITCH)
        }
    }

    companion object {

        fun loadMarker(view: View, listId: String) {
//            if (listId != "" && colors != null && colors.containsKey(listId)) {
//                view.setBackgroundColor(ThemeUtil.getInstance(view.context).getNoteColor(colors.get(listId)))
//            }
        }

        fun loadTaskCard(cardView: CardView, i: Int) {
            cardView.setCardBackgroundColor(ThemeUtil.getInstance(cardView.context).cardStyle)
            if (Module.isLollipop) {
                cardView.cardElevation = Configs.CARD_ELEVATION
            }
        }

        fun loadCheck(checkBox: RoboCheckBox, item: GoogleTask) {
            checkBox.isChecked = item.status.matches(Google.TASKS_COMPLETE.toRegex())
        }

        fun loadDue(view: RoboTextView, due: Long) {
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
}

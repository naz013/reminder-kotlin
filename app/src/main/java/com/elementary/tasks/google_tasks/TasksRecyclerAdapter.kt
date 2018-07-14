package com.elementary.tasks.google_tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.views.roboto.RoboCheckBox
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.elementary.tasks.databinding.ListItemTaskBinding

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

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

        var binding: ListItemTaskBinding? = null

        init {
            binding = DataBindingUtil.bind(v)
            binding!!.setClick { v1 -> onItemClick(adapterPosition) }
            binding!!.checkDone.setOnClickListener { view -> switchTask(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding!!.googleTask = googleTasks[position]
    }

    override fun getItemCount(): Int {
        return googleTasks.size
    }

    private fun onItemClick(position: Int) {
        if (actionsListener != null) {
            actionsListener!!.onAction(null, position, googleTasks[position], ListActions.EDIT)
        }
    }

    private fun switchTask(position: Int) {
        if (actionsListener != null) {
            actionsListener!!.onAction(null, position, googleTasks[position], ListActions.SWITCH)
        }
    }

    companion object {

        @BindingAdapter("loadMarker")
        fun loadMarker(view: View, listId: String) {
            //        if (listId != null && colors != null && colors.containsKey(listId)) {
            //            view.setBackgroundColor(ThemeUtil.getInstance(view.getContext()).getNoteColor(colors.get(listId)));
            //        }
        }

        @BindingAdapter("loadTaskCard")
        fun loadTaskCard(cardView: CardView, i: Int) {
            cardView.setCardBackgroundColor(ThemeUtil.getInstance(cardView.context).cardStyle)
            if (Module.isLollipop) {
                cardView.cardElevation = Configs.CARD_ELEVATION
            }
        }

        @BindingAdapter("loadCheck")
        fun loadCheck(checkBox: RoboCheckBox, item: GoogleTask) {
            if (item.status!!.matches(Google.TASKS_COMPLETE.toRegex())) {
                checkBox.isChecked = true
            } else {
                checkBox.isChecked = false
            }
        }

        @BindingAdapter("loadDue")
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
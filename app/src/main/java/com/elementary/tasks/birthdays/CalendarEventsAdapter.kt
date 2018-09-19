package com.elementary.tasks.birthdays

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.reminder.lists.adapter.ReminderHolder
import com.elementary.tasks.reminder.lists.adapter.ShoppingHolder
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
class CalendarEventsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: List<EventsItem> = ArrayList()
    private var mEventListener: ActionsListener<EventsItem>? = null

    fun setData(data: List<EventsItem>) {
        this.data = data
        notifyDataSetChanged()
    }

    fun setEventListener(listener: ActionsListener<EventsItem>?) {
        this.mEventListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> ReminderHolder(parent, { view, i, listActions ->
                mEventListener?.onAction(view, i, data[i], listActions)
            }, false)
            1 -> ShoppingHolder(parent, { view, i, listActions ->
                mEventListener?.onAction(view, i, data[i], listActions)
            }, false)
            else -> BirthdayHolder(parent) { view, i, listActions ->
                mEventListener?.onAction(view, i, data[i], listActions)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BirthdayHolder -> {
                val item = data[position].`object` as Birthday
                holder.setData(item)
                holder.setColor(data[position].color)
            }
            is ReminderHolder -> {
                val item = data[position].`object` as Reminder
                holder.setData(item)
            }
            is ShoppingHolder -> {
                val item = data[position].`object` as Reminder
                holder.setData(item)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].viewType
    }

    fun getItem(position: Int): AdapterItem {
        return data[position]
    }
}

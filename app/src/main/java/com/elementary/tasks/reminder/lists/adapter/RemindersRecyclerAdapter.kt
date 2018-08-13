package com.elementary.tasks.reminder.lists.adapter

import android.app.AlarmManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.TimeUtil

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
class RemindersRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var actionsListener: ActionsListener<Reminder>? = null
    private var isEditable = true
    private val mData = mutableListOf<Reminder>()

    var data: List<Reminder>
        get() = mData
        set(list) {
            this.mData.clear()
            this.mData.addAll(list)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun setEditable(editable: Boolean) {
        isEditable = editable
    }

    fun getItem(position: Int): Reminder {
        return mData[position]
    }

    private fun initLabel(listHeader: TextView, position: Int) {
        val item = getItem(position)
        val due = TimeUtil.getDateTimeFromGmt(item.eventTime)
        var simpleDate = TimeUtil.getSimpleDate(due)
        var prevItem: Reminder? = null
        try {
            prevItem = getItem(position - 1)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
        }

        val context = listHeader.context
        if (!item.isActive && position > 0 && prevItem != null && prevItem.isActive) {
            simpleDate = context.getString(R.string.disabled)
            listHeader.text = simpleDate
            listHeader.visibility = View.VISIBLE
        } else if (!item.isActive && position > 0 && prevItem != null && !prevItem.isActive) {
            listHeader.visibility = View.GONE
        } else if (!item.isActive && position == 0) {
            simpleDate = context.getString(R.string.disabled)
            listHeader.text = simpleDate
            listHeader.visibility = View.VISIBLE
        } else if (item.isActive && position > 0 && prevItem != null && simpleDate == TimeUtil.getSimpleDate(prevItem.eventTime)) {
            listHeader.visibility = View.GONE
        } else {
            if (due <= 0 || due < System.currentTimeMillis() - AlarmManager.INTERVAL_DAY) {
                simpleDate = context.getString(R.string.permanent)
            } else {
                if (simpleDate == TimeUtil.getSimpleDate(System.currentTimeMillis())) {
                    simpleDate = context.getString(R.string.today)
                } else if (simpleDate == TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY)) {
                    simpleDate = context.getString(R.string.tomorrow)
                }
            }
            listHeader.text = simpleDate
            listHeader.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == Reminder.REMINDER) {
            ReminderHolder(parent, { view, i, listActions ->
                actionsListener?.onAction(view, i, getItem(i), listActions)
            }, isEditable)
        } else {
            ShoppingHolder(parent) { view, i, listActions ->
                actionsListener?.onAction(view, i, getItem(i), listActions)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is ReminderHolder) {
            holder.setData(item)
            if (isEditable) {
                initLabel(holder.listHeader, position)
            }
        } else if (holder is ShoppingHolder) {
            holder.setData(item)
            if (isEditable) {
                initLabel(holder.listHeader, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return item.viewType
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return item.uniqueId.toLong()
    }
}

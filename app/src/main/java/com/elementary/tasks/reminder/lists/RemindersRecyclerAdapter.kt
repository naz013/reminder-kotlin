package com.elementary.tasks.reminder.lists

import android.app.AlarmManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.elementary.tasks.databinding.ListItemReminderBinding
import com.elementary.tasks.databinding.ListItemShoppingBinding

import java.util.ArrayList
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
class RemindersRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mEventListener: RecyclerListener? = null
    private var isEditable = true
    private val mData = ArrayList<Reminder>()

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

    fun getItem(position: Int): Reminder? {
        return if (position >= 0 && position < mData.size) mData[position] else null
    }

    fun removeItem(position: Int) {
        if (position < mData.size) {
            mData.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(0, mData.size)
        }
    }

    private fun initLabel(listHeader: RoboTextView, position: Int) {
        val item = getItem(position) ?: return
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
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == Reminder.REMINDER) {
            ReminderHolder(ListItemReminderBinding.inflate(inflater, parent, false).root, mEventListener, isEditable)
        } else {
            ShoppingHolder(ListItemShoppingBinding.inflate(inflater, parent, false).root, mEventListener)
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
        val item = getItem(position) ?: return 0
        return item.viewType
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position) ?: return 0
        return item.uniqueId.toLong()
    }

    fun setEventListener(eventListener: RecyclerListener) {
        mEventListener = eventListener
    }
}

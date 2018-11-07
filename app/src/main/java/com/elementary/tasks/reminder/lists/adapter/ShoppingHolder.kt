package com.elementary.tasks.reminder.lists.adapter

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.list_item_reminder.view.*
import kotlinx.android.synthetic.main.list_item_task_item_widget.view.*
import javax.inject.Inject

/**
 * Copyright 2017 Nazar Suhovich
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
class ShoppingHolder(parent: ViewGroup, private val listener: ((View, Int, ListActions) -> Unit)?, val editable: Boolean) :
        BaseHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_reminder, parent, false)) {

    val listHeader: TextView = itemView.listHeader

    @Inject
    lateinit var reminderUtils: ReminderUtils

    init {
        ReminderApp.appComponent.inject(this)
        if (editable) {
            itemView.itemCheck.visibility = View.VISIBLE
        } else {
            itemView.itemCheck.visibility = View.GONE
        }
        itemView.reminder_phone.visibility = View.GONE
        itemView.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
        itemView.button_more.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
        itemView.itemCheck.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.SWITCH) }
    }

    fun setData(reminder: Reminder) {
        itemView.taskText.text = reminder.summary
        loadCheck(reminder)
        loadContainer(reminder.type)
        loadType(reminder.type)
        loadPriority(reminder.priority)
        loadGroup(reminder)
        loadShoppingDate(reminder)
        loadItems(reminder.shoppings)
    }

    private fun loadCheck(item: Reminder?) {
        if (item == null || item.isRemoved) {
            itemView.itemCheck.visibility = View.GONE
            return
        }
        itemView.itemCheck.isChecked = item.isActive
    }

    private fun loadContainer(type: Int) {
        if (Reminder.isBase(type, Reminder.BY_LOCATION) || Reminder.isBase(type, Reminder.BY_OUT) || Reminder.isBase(type, Reminder.BY_PLACES)) {
            itemView.endContainer.visibility = View.GONE
        } else {
            itemView.endContainer.visibility = View.VISIBLE
        }
    }

    private fun loadGroup(reminder: Reminder) {
        val colorStateList = ColorStateList.valueOf(themeUtil.getColor(themeUtil.getCategoryColor(reminder.groupColor)))
        itemView.chipPriority.chipStrokeColor = colorStateList
        itemView.chipType.chipStrokeColor = colorStateList
        itemView.chipGroup.chipStrokeColor = colorStateList
        itemView.chipGroup.text = reminder.groupTitle
    }

    private fun loadPriority(type: Int) {
        itemView.chipPriority.text = reminderUtils.getPriorityTitle(type)
    }

    private fun loadType(type: Int) {
        itemView.chipType.text = reminderUtils.getTypeString(type)
    }

    private fun loadLeft(item: Reminder) {
        if (item.isActive && !item.isRemoved) {
            itemView.remainingTime.text = TimeCount.getRemaining(itemView.context, item.eventTime, item.delay)
        } else {
            itemView.remainingTime.text = ""
        }
    }

    private fun loadItems(shoppings: List<ShopItem>) {
        val isDark = themeUtil.isDark
        itemView.todoList.visibility = View.VISIBLE
        itemView.todoList.isFocusableInTouchMode = false
        itemView.todoList.isFocusable = false
        itemView.todoList.removeAllViewsInLayout()
        var count = 0
        for (list in shoppings) {
            val bind = LayoutInflater.from(itemView.todoList.context).inflate(R.layout.list_item_task_item_widget, itemView.todoList, false)
            val checkView = bind.checkView
            val textView = bind.shopText
            if (list.isChecked) {
                if (isDark) {
                    checkView.setImageResource(R.drawable.ic_check_box_white_24dp)
                } else {
                    checkView.setImageResource(R.drawable.ic_check_box_black_24dp)
                }
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                if (isDark) {
                    checkView.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp)
                } else {
                    checkView.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp)
                }
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            count++
            if (count == 9) {
                checkView.visibility = View.INVISIBLE
                textView.text = "..."
                itemView.todoList.addView(bind)
                break
            } else {
                checkView.visibility = View.VISIBLE
                textView.text = list.summary
                itemView.todoList.addView(bind)
            }
        }
    }

    private fun loadShoppingDate(reminder: Reminder) {
        val is24 = prefs.is24HourFormatEnabled
        val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        if (due > 0) {
            itemView.taskDate.text = TimeUtil.getFullDateTime(due, is24, false)
            itemView.taskDate.visibility = View.VISIBLE
            loadLeft(reminder)
        } else {
            itemView.taskDate.visibility = View.GONE
            itemView.endContainer.visibility = View.GONE
        }
    }
}

package com.elementary.tasks.reminder.lists

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.TimeUtil
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.list_item_shopping.view.*
import kotlinx.android.synthetic.main.list_item_task_item_widget.view.*

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
class ShoppingHolder(parent: ViewGroup, private val listener: ((View, Int, ListActions) -> Unit)?) :
        BaseHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_shopping, parent, false)) {

    val listHeader: TextView = itemView.listHeader

    init {
        itemView.itemCard.onClick { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
        itemView.button_more.onClick { listener?.invoke(it, adapterPosition, ListActions.MORE) }
    }

    fun setData(reminder: Reminder) {
        itemView.shoppingTitle.text = reminder.summary
        loadShoppingDate(reminder.eventTime)
        loadCard(reminder.group)
        loadItems(reminder.shoppings)
    }

    private fun loadItems(shoppings: List<ShopItem>) {
        val isDark = themeUtil.isDark
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

    private fun loadShoppingDate(eventTime: String?) {
        val is24 = prefs.is24HourFormatEnabled
        val due = TimeUtil.getDateTimeFromGmt(eventTime)
        if (due > 0) {
            itemView.shoppingTime.text = TimeUtil.getFullDateTime(due, is24, false)
            itemView.shoppingTime.visibility = View.VISIBLE
        } else {
            itemView.shoppingTime.visibility = View.GONE
        }
    }

    private fun loadCard(group: Group?) {
        if (group != null) {
            itemView.itemCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.getCategoryColor(group.color)))
        } else {
            itemView.itemCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.getCategoryColor(0)))
        }
    }
}

package com.elementary.tasks.reminder.lists

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView

import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.elementary.tasks.databinding.ListItemShoppingBinding
import com.elementary.tasks.databinding.ListItemTaskItemWidgetBinding

import javax.inject.Inject
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

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
class ShoppingHolder(v: View, private val mEventListener: RecyclerListener?) : RecyclerView.ViewHolder(v) {

    val listHeader: RoboTextView
    private val binding: ListItemShoppingBinding?
    @Inject
    var themeUtil: ThemeUtil? = null

    init {
        ReminderApp.appComponent!!.inject(this)
        binding = DataBindingUtil.bind(v)
        listHeader = binding!!.listHeader
        if (mEventListener != null) {
            binding.setClick { v1 ->
                when (v1.id) {
                    R.id.itemCheck -> mEventListener.onItemSwitched(adapterPosition, v1)
                    else -> mEventListener.onItemClicked(adapterPosition, v1)
                }
            }
        }
    }

    fun setData(reminder: Reminder) {
        binding!!.item = reminder
        loadShoppingDate(reminder.eventTime)
        loadCard(reminder.group)
        loadItems(reminder.shoppings)
    }

    private fun loadItems(shoppings: List<ShopItem>) {
        val isDark = themeUtil!!.isDark
        binding!!.todoList.isFocusableInTouchMode = false
        binding.todoList.isFocusable = false
        binding.todoList.removeAllViewsInLayout()
        var count = 0
        for (list in shoppings) {
            val bind = ListItemTaskItemWidgetBinding.inflate(LayoutInflater.from(binding.todoList.context), null, false)
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
                binding.todoList.addView(bind.root)
                break
            } else {
                checkView.visibility = View.VISIBLE
                textView.text = list.summary
                binding.todoList.addView(bind.root)
            }
        }
    }

    private fun loadShoppingDate(eventTime: String?) {
        val is24 = Prefs.getInstance(binding!!.shoppingTime.context).is24HourFormatEnabled
        val due = TimeUtil.getDateTimeFromGmt(eventTime)
        if (due > 0) {
            binding.shoppingTime.text = TimeUtil.getFullDateTime(due, is24, false)
            binding.shoppingTime.visibility = View.VISIBLE
        } else {
            binding.shoppingTime.visibility = View.GONE
        }
    }

    private fun loadCard(group: Group?) {
        if (group != null) {
            binding!!.itemCard.setCardBackgroundColor(themeUtil!!.getColor(themeUtil!!.getCategoryColor(group.color)))
        } else {
            binding!!.itemCard.setCardBackgroundColor(themeUtil!!.getColor(themeUtil!!.getCategoryColor(0)))
        }
    }
}

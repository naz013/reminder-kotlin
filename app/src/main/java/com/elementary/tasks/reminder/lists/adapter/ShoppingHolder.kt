package com.elementary.tasks.reminder.lists.adapter

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.databinding.ListItemReminderBinding
import com.elementary.tasks.databinding.ListItemShopItemBinding
import org.koin.standalone.inject

class ShoppingHolder(parent: ViewGroup, val editable: Boolean, showMore: Boolean = true,
                     private val listener: ((View, Int, ListActions) -> Unit)? = null) :
        BaseHolder<ListItemReminderBinding>(parent, R.layout.list_item_reminder) {

    private val themeUtil: ThemeUtil by inject()
    val listHeader: TextView = binding.listHeader

    init {
        if (editable) {
            binding.itemCheck.visibility = View.VISIBLE
        } else {
            binding.itemCheck.visibility = View.GONE
        }
        binding.reminderPhone.visibility = View.GONE
        binding.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
        binding.itemCheck.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.SWITCH) }

        if (showMore) {
            binding.buttonMore.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
            binding.buttonMore.visibility = View.VISIBLE
        } else {
            binding.buttonMore.visibility = View.GONE
        }
    }

    fun setData(reminder: Reminder) {
        binding.taskText.text = reminder.summary
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
            binding.itemCheck.visibility = View.GONE
            return
        }
        binding.itemCheck.isChecked = item.isActive
    }

    private fun loadContainer(type: Int) {
        if (Reminder.isBase(type, Reminder.BY_LOCATION) || Reminder.isBase(type, Reminder.BY_OUT) || Reminder.isBase(type, Reminder.BY_PLACES)) {
            binding.endContainer.visibility = View.GONE
        } else {
            binding.endContainer.visibility = View.VISIBLE
        }
    }

    private fun loadGroup(reminder: Reminder) {
        val colorStateList = ColorStateList.valueOf(themeUtil.getCategoryColor(reminder.groupColor))
        binding.chipPriority.chipStrokeColor = colorStateList
        binding.chipType.chipStrokeColor = colorStateList
        binding.chipGroup.chipStrokeColor = colorStateList
        binding.chipGroup.text = reminder.groupTitle
    }

    private fun loadPriority(type: Int) {
        binding.chipPriority.text = ReminderUtils.getPriorityTitle(itemView.context, type)
    }

    private fun loadType(type: Int) {
        binding.chipType.text = ReminderUtils.getTypeString(itemView.context, type)
    }

    private fun loadLeft(item: Reminder) {
        if (item.isActive && !item.isRemoved) {
            binding.remainingTime.text = TimeCount.getRemaining(itemView.context, item.eventTime, item.delay, prefs.appLanguage)
        } else {
            binding.remainingTime.text = ""
        }
    }

    private fun loadItems(shoppings: List<ShopItem>) {
        val isDark = themeUtil.isDark
        binding.todoList.visibility = View.VISIBLE
        binding.todoList.isFocusableInTouchMode = false
        binding.todoList.isFocusable = false
        binding.todoList.removeAllViewsInLayout()
        var count = 0
        for (list in shoppings) {
            val bind = ListItemShopItemBinding.inflate(LayoutInflater.from(binding.todoList.context), binding.todoList, false)
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

    private fun loadShoppingDate(reminder: Reminder) {
        val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        if (due > 0) {
            binding.taskDate.text = TimeUtil.getFullDateTime(due, prefs.is24HourFormat, prefs.appLanguage)
            binding.taskDate.visibility = View.VISIBLE
            loadLeft(reminder)
        } else {
            binding.taskDate.visibility = View.GONE
            binding.endContainer.visibility = View.GONE
        }
    }
}

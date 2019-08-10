package com.elementary.tasks.reminder.lists.adapter

import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableStringBuilder
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

class ShoppingHolder(parent: ViewGroup, val editable: Boolean, showMore: Boolean = true,
                     private val listener: ((View, Int, ListActions) -> Unit)? = null) :
        BaseHolder<ListItemReminderBinding>(parent, R.layout.list_item_reminder) {

    val listHeader: TextView = binding.listHeader

    init {
        if (editable) {
            binding.itemCheck.show()
        } else {
            binding.itemCheck.hide()
        }
        binding.reminderPhone.hide()
        binding.itemCard.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.OPEN) }
        binding.itemCheck.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.SWITCH) }

        if (showMore) {
            binding.buttonMore.setOnClickListener { listener?.invoke(it, adapterPosition, ListActions.MORE) }
            binding.buttonMore.show()
        } else {
            binding.buttonMore.hide()
        }
    }

    fun setData(reminder: Reminder) {
        binding.taskText.text = reminder.summary
        loadCheck(reminder)
        loadGroup(reminder)
        loadShoppingDate(reminder)
        loadItems(reminder.shoppings)
    }

    private fun loadCheck(item: Reminder?) {
        if (item == null || item.isRemoved) {
            binding.itemCheck.hide()
            return
        }
        binding.itemCheck.isChecked = item.isActive
    }

    private fun loadGroup(reminder: Reminder) {
        val priority = ReminderUtils.getPriorityTitle(itemView.context, reminder.priority)
        val typeLabel = ReminderUtils.getTypeString(itemView.context, reminder.type)
        binding.reminderTypeGroup.text = "$typeLabel (${reminder.groupTitle}, $priority)"
    }

    private fun loadLeft(item: Reminder) {
        if (item.isActive && !item.isRemoved) {
            val context = binding.reminderRepeatLeft.context
            val spannableStringBuilder = SpannableStringBuilder()
            val remainingText = TimeCount.getRemaining(itemView.context, item.eventTime, item.delay, prefs.appLanguage)
            spannableStringBuilder.append("!!!$remainingText")
            spannableStringBuilder.setSpan(CenteredImageSpan(context, R.drawable.ic_twotone_repeat_24px),
                    0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            binding.reminderRepeatLeft.text = spannableStringBuilder
        } else {
            binding.reminderRepeatLeft.text = ""
        }
    }

    private fun loadItems(shoppings: List<ShopItem>) {
        val isDark = ThemeUtil.isDarkMode(itemView.context)
        binding.todoList.show()
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
                checkView.transparent()
                textView.text = "..."
                binding.todoList.addView(bind.root)
                break
            } else {
                checkView.show()
                textView.text = list.summary
                binding.todoList.addView(bind.root)
            }
        }
    }

    private fun loadShoppingDate(reminder: Reminder) {
        val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        if (due > 0) {
            binding.taskDate.text = TimeUtil.getFullDateTime(due, prefs.is24HourFormat, prefs.appLanguage)
            binding.taskDate.show()
            if (reminder.isActive && !reminder.isRemoved) {
                loadLeft(reminder)
            } else {
                binding.reminderRepeatLeft.hide()
            }
        } else {
            binding.taskDate.hide()
            binding.reminderRepeatLeft.hide()
        }
    }
}

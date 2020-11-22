package com.elementary.tasks.reminder.lists.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ListItemParams
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.dp2px
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.views.TextDrawable
import com.elementary.tasks.databinding.ListItemReminderBinding
import com.elementary.tasks.databinding.ListItemShopItemBinding

class ShoppingHolder(
  parent: ViewGroup,
  prefs: Prefs,
  val editable: Boolean,
  showMore: Boolean = true,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseHolder<ListItemReminderBinding>(parent, R.layout.list_item_reminder, prefs) {

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

  private fun loadLeft(reminder: Reminder) {
    binding.badgesView.show()
    if (reminder.isActive && !reminder.isRemoved) {
      val context = binding.itemCard.context
      val remainingText = TimeCount.getRemaining(
        context,
        reminder.eventTime,
        reminder.delay,
        prefs.appLanguage
      )
      binding.timeToBadge.setImageDrawable(
        createBadge(context, remainingText, context.dp2px(ListItemParams.BADGE_WIDTH_INCREASED_DP))
      )
      binding.timeToBadge.show()
    } else {
      binding.timeToBadge.hide()
    }
  }

  private fun createBadge(
    context: Context,
    text: String,
    width: Int = context.dp2px(ListItemParams.BADGE_WIDTH_DP),
    backgroundColor: Int = ThemeUtil.getSecondaryColor(context),
    textColor: Int = ThemeUtil.getOnSecondaryColor(context)
  ): TextDrawable {
    return TextDrawable.builder()
      .beginConfig()
      .textColor(textColor)
      .height(context.dp2px(ListItemParams.BADGE_HEIGHT_DP))
      .width(width)
      .toUpperCase()
      .bold()
      .endConfig()
      .buildRoundRect(text, backgroundColor, context.dp2px(ListItemParams.BADGE_CORNERS_DP))
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
        binding.badgesView.hide()
      }
    } else {
      binding.taskDate.hide()
      binding.badgesView.hide()
    }
  }
}

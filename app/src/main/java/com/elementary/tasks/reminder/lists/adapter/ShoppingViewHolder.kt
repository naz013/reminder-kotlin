package com.elementary.tasks.reminder.lists.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.ListItemReminderBinding
import com.elementary.tasks.databinding.ListItemShopItemBinding

class ShoppingViewHolder(
  parent: ViewGroup,
  editable: Boolean,
  showMore: Boolean = true,
  private val isDark: Boolean,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseUiReminderListViewHolder<ListItemReminderBinding, UiReminderListActiveShop>(
  ListItemReminderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    if (editable) {
      binding.itemCheck.visible()
    } else {
      binding.itemCheck.gone()
    }
    binding.reminderPhone.gone()
    binding.itemCard.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.OPEN
      )
    }
    binding.itemCheck.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.SWITCH
      )
    }

    if (showMore) {
      binding.buttonMore.setOnClickListener {
        listener?.invoke(
          it,
          bindingAdapterPosition,
          ListActions.MORE
        )
      }
      binding.buttonMore.visible()
    } else {
      binding.buttonMore.gone()
    }
  }

  override fun setData(data: UiReminderListActiveShop) {
    binding.taskText.text = data.summary
    loadCheck(data)
    loadGroup(data)
    loadShoppingDate(data)
    loadLeft(data)
    loadItems(data.shopList)
  }

  private fun loadCheck(reminder: UiReminderListActiveShop) {
    binding.itemCheck.isChecked = reminder.status.active
  }

  @SuppressLint("SetTextI18n")
  private fun loadGroup(reminder: UiReminderListActiveShop) {
    val priority = reminder.priority
    val typeLabel = reminder.illustration.title
    val groupName = reminder.group?.title ?: ""
    binding.reminderTypeGroup.text = "$typeLabel ($groupName, $priority)"
  }

  private fun loadLeft(reminder: UiReminderListActiveShop) {
    val due = reminder.due.dateTime
    binding.badgesView.visibleGone(due != null)
    binding.timeToBadge.visibleGone(reminder.isRunning)
    binding.timeToBadge.text = reminder.due.remaining
  }

  private fun loadItems(shoppings: List<ShopItem>) {
    binding.todoList.visible()
    binding.todoList.isFocusableInTouchMode = false
    binding.todoList.isFocusable = false
    binding.todoList.removeAllViewsInLayout()
    var count = 0
    for (list in shoppings) {
      val bind = ListItemShopItemBinding.inflate(
        LayoutInflater.from(binding.todoList.context),
        binding.todoList,
        false
      )
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
        checkView.visible()
        textView.text = list.summary
        binding.todoList.addView(bind.root)
      }
    }
  }

  private fun loadShoppingDate(reminder: UiReminderListActiveShop) {
    val due = reminder.due.dateTime
    if (due != null) {
      binding.taskDate.text = reminder.due.dateTime
      binding.taskDate.visible()
    } else {
      binding.taskDate.gone()
      binding.badgesView.gone()
    }
  }
}

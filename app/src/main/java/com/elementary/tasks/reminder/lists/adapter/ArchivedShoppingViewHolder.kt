package com.elementary.tasks.reminder.lists.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.UiReminderListRemovedShop
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.core.utils.ui.transparent
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.databinding.ListItemReminderBinding
import com.elementary.tasks.databinding.ListItemShopItemBinding

class ArchivedShoppingViewHolder(
  parent: ViewGroup,
  showMore: Boolean = true,
  private val isDark: Boolean,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseUiReminderListViewHolder<ListItemReminderBinding, UiReminderListRemovedShop>(
  ListItemReminderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.switchWrapper.gone()
    binding.reminderPhone.gone()
    binding.itemCard.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.OPEN
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

  override fun setData(data: UiReminderListRemovedShop) {
    binding.taskText.text = data.summary
    loadGroup(data)
    loadShoppingDate(data)
    loadItems(data.shopList)
  }

  @SuppressLint("SetTextI18n")
  private fun loadGroup(reminder: UiReminderListRemovedShop) {
    val priority = reminder.priority
    val typeLabel = reminder.title
    val groupName = reminder.group?.title ?: ""
    binding.reminderTypeGroup.text = "$typeLabel ($groupName, $priority)"
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
        checkView.setImageResource(R.drawable.ic_fluent_checkbox_checked)
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
      } else {
        checkView.setImageResource(R.drawable.ic_fluent_checkbox_unchecked)
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

  private fun loadShoppingDate(reminder: UiReminderListRemovedShop) {
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

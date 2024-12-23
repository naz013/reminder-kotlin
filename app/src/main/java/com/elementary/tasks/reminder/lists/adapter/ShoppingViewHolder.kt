package com.elementary.tasks.reminder.lists.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.inflater
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import com.elementary.tasks.databinding.ListItemReminderBinding
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleReminderViewHolderCommon

class ShoppingViewHolder(
  parent: ViewGroup,
  editable: Boolean,
  showMore: Boolean = true,
  private val isDark: Boolean,
  private val scheduleReminderViewHolderCommon: ScheduleReminderViewHolderCommon,
  private val listener: ((View, Int, ListActions) -> Unit)? = null
) : BaseUiReminderListViewHolder<ListItemReminderBinding, UiReminderListActiveShop>(
  ListItemReminderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.switchWrapper.visibleGone(editable)
    binding.reminderPhone.gone()
    binding.itemCard.setOnClickListener {
      listener?.invoke(
        it,
        bindingAdapterPosition,
        ListActions.OPEN
      )
    }
    binding.switchWrapper.setOnClickListener {
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
    scheduleReminderViewHolderCommon.loadItems(
      reminder = data,
      todoListView = binding.todoList,
      isDark = isDark,
      textColor = ThemeProvider.getThemeOnSurfaceColor(itemView.context)
    )
  }

  private fun loadCheck(reminder: UiReminderListActiveShop) {
    binding.itemCheck.isChecked = reminder.status.active
  }

  @SuppressLint("SetTextI18n")
  private fun loadGroup(reminder: UiReminderListActiveShop) {
    val priority = reminder.priority
    val typeLabel = reminder.title
    val groupName = reminder.group?.title ?: ""
    binding.reminderTypeGroup.text = "$typeLabel ($groupName, $priority)"
  }

  private fun loadLeft(reminder: UiReminderListActiveShop) {
    val due = reminder.due.dateTime
    binding.badgesView.visibleGone(due != null)
    binding.timeToBadge.visibleGone(reminder.isRunning)
    binding.timeToBadge.text = reminder.due.remaining
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

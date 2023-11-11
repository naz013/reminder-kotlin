package com.elementary.tasks.home.scheduleview.viewholder

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemScheduleReminderBinding
import com.elementary.tasks.reminder.lists.adapter.BaseUiReminderListViewHolder

class ScheduleShoppingViewHolder(
  parent: ViewGroup,
  private val common: ScheduleReminderViewHolderCommon,
  private val isDark: Boolean,
  private val listener: (Int) -> Unit
) : BaseUiReminderListViewHolder<ListItemScheduleReminderBinding, UiReminderListActiveShop>(
  ListItemScheduleReminderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.eventPhoneView.gone()
    binding.itemCard.setOnClickListener {
      listener(bindingAdapterPosition)
    }
  }

  override fun setData(data: UiReminderListActiveShop) {
    binding.eventTextView.text = data.summary
    binding.eventTimeView.text = data.due.formattedTime
    common.loadItems(
      reminder = data,
      todoListView = binding.todoListView,
      isDark = isDark,
      textColor = ThemeProvider.getThemeOnSurfaceColor(itemView.context)
    )
    data.group?.also { group ->
      binding.eventIconView.imageTintList = ColorStateList.valueOf(group.color)
    }
  }
}

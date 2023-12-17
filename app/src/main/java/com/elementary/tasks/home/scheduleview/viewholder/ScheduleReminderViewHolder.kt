package com.elementary.tasks.home.scheduleview.viewholder

import android.content.res.ColorStateList
import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemScheduleReminderBinding
import com.elementary.tasks.reminder.lists.adapter.BaseUiReminderListViewHolder

class ScheduleReminderViewHolder(
  parent: ViewGroup,
  private val common: ScheduleReminderViewHolderCommon,
  private val listener: (Int) -> Unit
) : BaseUiReminderListViewHolder<ListItemScheduleReminderBinding, UiReminderListActive>(
  ListItemScheduleReminderBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.todoListView.gone()
    binding.itemCard.setOnClickListener {
      listener(bindingAdapterPosition)
    }
  }

  override fun setData(data: UiReminderListActive) {
    binding.eventTextView.text = data.summary
    binding.eventTimeView.text = data.due.formattedTime
    common.loadContact(data, binding.eventPhoneView)
    data.group?.also { group ->
      binding.eventIconView.imageTintList = ColorStateList.valueOf(group.color)
    }
  }
}

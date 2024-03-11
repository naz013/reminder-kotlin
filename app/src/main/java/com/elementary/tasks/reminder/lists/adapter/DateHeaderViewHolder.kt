package com.elementary.tasks.reminder.lists.adapter

import android.view.ViewGroup
import com.elementary.tasks.core.data.ui.UiReminderListHeader
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.databinding.ListItemReminderHeaderBinding

class DateHeaderViewHolder(
  parent: ViewGroup
) : BaseUiReminderListViewHolder<ListItemReminderHeaderBinding, UiReminderListHeader>(
  ListItemReminderHeaderBinding.inflate(parent.inflater(), parent, false)
) {

  override fun setData(data: UiReminderListHeader) {
    binding.dateView.text = data.date
  }
}

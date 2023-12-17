package com.elementary.tasks.reminder.lists.adapter

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.home.scheduleview.viewholder.BaseScheduleHolder

abstract class BaseUiReminderListViewHolder<B : ViewBinding, T : UiReminderList>(
  binding: B
) : BaseScheduleHolder<B>(binding) {

  abstract fun setData(data: T)
}

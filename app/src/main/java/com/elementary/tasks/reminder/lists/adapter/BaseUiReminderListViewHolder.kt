package com.elementary.tasks.reminder.lists.adapter

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.UiReminderList

abstract class BaseUiReminderListViewHolder<B : ViewBinding, T : UiReminderList>(
  binding: B,
) : HolderBinding<B>(binding) {

  abstract fun setData(data: T)
}

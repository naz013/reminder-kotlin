package com.elementary.tasks.reminder.preview.adapter

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.inflater
import com.github.naz013.ui.common.view.visible
import com.elementary.tasks.databinding.ListItemGoogleCalendarEventBinding
import com.elementary.tasks.reminder.preview.data.UiCalendarEventList

class GoogleEventViewHolder(
  parent: ViewGroup,
  listener: ((View, Int, ListActions) -> Unit)?
) : HolderBinding<ListItemGoogleCalendarEventBinding>(
  ListItemGoogleCalendarEventBinding.inflate(parent.inflater(), parent, false)
) {

  init {
    binding.viewButton.setOnClickListener {
      listener?.invoke(it, bindingAdapterPosition, ListActions.OPEN)
    }
    binding.buttonDelete.setOnClickListener {
      listener?.invoke(it, bindingAdapterPosition, ListActions.REMOVE)
    }
  }

  fun bind(eventItem: UiCalendarEventList) {
    binding.task.text = eventItem.title
    binding.note.text = eventItem.description
    if (eventItem.calendarName.isNullOrEmpty()) {
      binding.calendarName.gone()
    } else {
      binding.calendarName.text = eventItem.calendarName
      binding.calendarName.visible()
    }
    eventItem.dateStartFormatted?.run {
      binding.dtStart.text = this
      binding.dtStart.visible()
    } ?: run {
      binding.dtStart.gone()
    }
    eventItem.dateEndFormatted?.run {
      binding.dtEnd.text = this
      binding.dtEnd.visible()
    } ?: run {
      binding.dtEnd.gone()
    }
  }
}

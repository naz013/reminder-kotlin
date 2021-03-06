package com.elementary.tasks.reminder.preview

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.databinding.ListItemGoogleCalendarEventBinding

class GoogleEventViewHolder(
  parent: ViewGroup,
  currentStateHolder: CurrentStateHolder,
  listener: ((View, CalendarUtils.EventItem?, ListActions) -> Unit)?
) : BaseViewHolder<ListItemGoogleCalendarEventBinding>(
  ListItemGoogleCalendarEventBinding.inflate(parent.inflater(), parent, false),
  currentStateHolder
) {
  private var eventItem: CalendarUtils.EventItem? = null

  init {
    binding.viewButton.setOnClickListener { listener?.invoke(it, eventItem, ListActions.OPEN) }
    binding.buttonDelete.setOnClickListener { listener?.invoke(it, eventItem, ListActions.REMOVE) }
  }

  fun bind(eventItem: CalendarUtils.EventItem) {
    this.eventItem = eventItem
    binding.task.text = eventItem.title
    binding.note.text = eventItem.description
    if (eventItem.calendarName.isEmpty()) {
      binding.calendarName.hide()
    } else {
      binding.calendarName.text = eventItem.calendarName
      binding.calendarName.show()
    }
    if (eventItem.dtStart != 0L) {
      binding.dtStart.text = TimeUtil.getFullDateTime(eventItem.dtStart, prefs.is24HourFormat, prefs.appLanguage)
    }
    if (eventItem.dtEnd != 0L) {
      binding.dtEnd.text = TimeUtil.getFullDateTime(eventItem.dtEnd, prefs.is24HourFormat, prefs.appLanguage)
    }
  }
}
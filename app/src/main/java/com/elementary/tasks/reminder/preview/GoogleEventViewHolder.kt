package com.elementary.tasks.reminder.preview

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.ListItemGoogleCalendarEventBinding

class GoogleEventViewHolder(
  parent: ViewGroup,
  currentStateHolder: CurrentStateHolder,
  private val dateTimeManager: DateTimeManager,
  listener: ((View, GoogleCalendarUtils.EventItem?, ListActions) -> Unit)?
) : BaseViewHolder<ListItemGoogleCalendarEventBinding>(
  ListItemGoogleCalendarEventBinding.inflate(parent.inflater(), parent, false),
  currentStateHolder
) {
  private var eventItem: GoogleCalendarUtils.EventItem? = null

  init {
    binding.viewButton.setOnClickListener { listener?.invoke(it, eventItem, ListActions.OPEN) }
    binding.buttonDelete.setOnClickListener { listener?.invoke(it, eventItem, ListActions.REMOVE) }
  }

  fun bind(eventItem: GoogleCalendarUtils.EventItem) {
    this.eventItem = eventItem
    binding.task.text = eventItem.title
    binding.note.text = eventItem.description
    if (eventItem.calendarName.isEmpty()) {
      binding.calendarName.gone()
    } else {
      binding.calendarName.text = eventItem.calendarName
      binding.calendarName.visible()
    }
    if (eventItem.dtStart != 0L) {
      binding.dtStart.text = dateTimeManager.getFullDateTime(eventItem.dtStart)
    }
    if (eventItem.dtEnd != 0L) {
      binding.dtEnd.text = dateTimeManager.getFullDateTime(eventItem.dtEnd)
    }
  }
}
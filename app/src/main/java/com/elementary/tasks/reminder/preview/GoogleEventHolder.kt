package com.elementary.tasks.reminder.preview

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.ListItemGoogleCalendarEventBinding

class GoogleEventHolder (parent: ViewGroup, listener: ((View, CalendarUtils.EventItem?, ListActions) -> Unit)?) :
        BaseHolder<ListItemGoogleCalendarEventBinding>(parent, R.layout.list_item_google_calendar_event) {
    private var eventItem: CalendarUtils.EventItem? = null

    init {
        binding.viewButton.setOnClickListener { listener?.invoke(it, eventItem, ListActions.OPEN) }
        binding.buttonDelete.setOnClickListener { listener?.invoke(it, eventItem, ListActions.REMOVE) }
    }

    fun bind(eventItem: CalendarUtils.EventItem) {
        this.eventItem = eventItem
        binding.task.text = eventItem.title
        binding.note.text = eventItem.description
        if (eventItem.dtStart != 0L) {
            binding.dtStart.text = TimeUtil.getFullDateTime(eventItem.dtStart, prefs.is24HourFormat, prefs.appLanguage)
        }
        if (eventItem.dtEnd != 0L) {
            binding.dtEnd.text = TimeUtil.getFullDateTime(eventItem.dtEnd, prefs.is24HourFormat, prefs.appLanguage)
        }
    }
}
package com.elementary.tasks.reminder.preview

import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.ListItemGoogleCalendarEventBinding

/**
 * Copyright 2018 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
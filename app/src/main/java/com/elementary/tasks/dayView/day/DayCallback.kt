package com.elementary.tasks.dayView.day

import com.elementary.tasks.core.view_models.day_view.DayViewViewModel
import com.elementary.tasks.dayView.EventsPagerItem

interface DayCallback {
    fun find(eventsPagerItem: EventsPagerItem, listener: ((EventsPagerItem, List<EventModel>) -> Unit)?)

    fun getViewModel(): DayViewViewModel
}
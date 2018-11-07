package com.elementary.tasks.dayView.day

import com.elementary.tasks.core.viewModels.dayVew.DayViewViewModel
import com.elementary.tasks.dayView.EventsPagerItem

interface DayCallback {
    fun find(eventsPagerItem: EventsPagerItem, listener: ((EventsPagerItem, List<EventModel>) -> Unit)?)

    fun getViewModel(): DayViewViewModel
}
package com.elementary.tasks.dayview.day

import com.elementary.tasks.dayview.DayViewViewModel
import com.elementary.tasks.dayview.EventsPagerItem

interface DayCallback {
  fun find(
    eventsPagerItem: EventsPagerItem,
    listener: ((EventsPagerItem, List<EventModel>) -> Unit)?
  )
  fun getViewModel(): DayViewViewModel
}

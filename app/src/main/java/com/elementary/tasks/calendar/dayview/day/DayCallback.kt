package com.elementary.tasks.calendar.dayview.day

import com.elementary.tasks.calendar.dayview.DayViewViewModel

interface DayCallback {
  fun getViewModel(): DayViewViewModel
}

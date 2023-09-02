package com.elementary.tasks.calendar

import com.elementary.tasks.calendar.dayview.DayViewViewModel
import com.elementary.tasks.calendar.dayview.weekheader.WeekFactory
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.calendar.monthview.CalendarViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val calendarModule = module {
  factory { WeekFactory(get(), get()) }
  factory { WeekHeaderController(get()) }

  single { CalendarDataProvider(get(), get(), get(), get(), get(), get(), get()) }

  viewModel { DayViewViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
  viewModel { CalendarViewModel(get(), get(), get(), get()) }
}

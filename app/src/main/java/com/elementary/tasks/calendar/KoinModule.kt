package com.elementary.tasks.calendar

import com.elementary.tasks.calendar.data.CalendarDataEngine
import com.elementary.tasks.calendar.data.CalendarDataEngineBroadcast
import com.elementary.tasks.calendar.data.DayLiveData
import com.elementary.tasks.calendar.data.MonthLiveData
import com.elementary.tasks.calendar.dayview.WeekViewModel
import com.elementary.tasks.calendar.dayview.day.DayViewModel
import com.elementary.tasks.calendar.dayview.weekheader.WeekFactory
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val calendarModule = module {
  factory { WeekFactory(get(), get()) }
  factory { WeekHeaderController(get()) }

  viewModel { WeekViewModel(get(), get()) }
  factory { DayViewModel(get(), get(), get(), get(), get(), get()) }

  single {
    CalendarDataEngine(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  factory { CalendarDataEngineBroadcast(get()) }

  factory { MonthLiveData(get(), get(), get(), get(), get()) }
  factory { DayLiveData(get(), get(), get(), get(), get()) }
}

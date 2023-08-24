package com.elementary.tasks.dayview

import com.elementary.tasks.dayview.weekheader.WeekFactory
import com.elementary.tasks.dayview.weekheader.WeekHeaderController
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dayViewModule = module {
  factory { WeekFactory(get(), get()) }
  factory { WeekHeaderController(get()) }

  single { DayViewProvider(get(), get(), get(), get()) }

  viewModel { DayViewViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

package com.elementary.tasks.monthview

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val monthViewModule = module {
  viewModel { CalendarViewModel(get(), get(), get(), get(), get(), get(), get()) }
}

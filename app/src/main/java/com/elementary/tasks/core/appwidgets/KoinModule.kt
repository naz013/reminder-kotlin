package com.elementary.tasks.core.appwidgets

import com.elementary.tasks.core.appwidgets.singlenote.SingleNoteWidgetConfigViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val widgetModule = module {
  single { WidgetPrefsHolder(get()) }

  viewModel { SingleNoteWidgetConfigViewModel(get(), get(), get(), get(), get()) }
}

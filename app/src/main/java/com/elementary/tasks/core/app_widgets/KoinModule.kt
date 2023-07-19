package com.elementary.tasks.core.app_widgets

import com.elementary.tasks.core.app_widgets.singlenote.SingleNoteWidgetConfigViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val widgetModule = module {
  single { WidgetPrefsHolder(get()) }

  viewModel { SingleNoteWidgetConfigViewModel(get(), get(), get(), get(), get()) }
}

package com.elementary.tasks.core.app_widgets

import org.koin.dsl.module

val widgetModule = module {
  single { WidgetPrefsHolder(get()) }
}

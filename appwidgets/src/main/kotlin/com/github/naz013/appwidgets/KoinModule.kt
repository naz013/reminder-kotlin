package com.github.naz013.appwidgets

import com.github.naz013.appwidgets.birthdays.UiBirthdayWidgetListAdapter
import com.github.naz013.appwidgets.calendar.WidgetDataProvider
import com.github.naz013.appwidgets.events.UiReminderWidgetListAdapter
import com.github.naz013.appwidgets.singlenote.SingleNoteWidgetConfigViewModel
import com.github.naz013.appwidgets.singlenote.data.UiNoteImagesAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteListSelectableAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteWidgetAdapter
import com.github.naz013.appwidgets.singlenote.adapter.RecyclableUiNoteWidgetAdapter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appWidgetsModule = module {
  single { WidgetPrefsHolder(get()) }

  factory { WidgetDataProvider(get(), get(), get(), get(), get()) }

  factory { UiBirthdayWidgetListAdapter(get(), get()) }
  factory { UiReminderWidgetListAdapter(get(), get(), get()) }

  factory { AppWidgetUpdaterImpl(get()) as AppWidgetUpdater }

  factory { UiNoteListSelectableAdapter(get(), get(), get(), get()) }
  factory { UiNoteImagesAdapter() }
  factory { RecyclableUiNoteWidgetAdapter(get(), get(), get(), get(), get()) }
  factory { UiNoteWidgetAdapter(get(), get(), get(), get(), get()) }

  viewModel { SingleNoteWidgetConfigViewModel(get(), get(), get(), get(), get(), get()) }
}

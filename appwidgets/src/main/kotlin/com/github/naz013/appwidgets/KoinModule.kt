package com.github.naz013.appwidgets

import com.github.naz013.appwidgets.birthdays.UiBirthdayWidgetListAdapter
import com.github.naz013.appwidgets.calendar.WidgetDataProvider
import com.github.naz013.appwidgets.events.EventsAppWidgetViewModel
import com.github.naz013.appwidgets.events.EventsWidgetPrefsProvider
import com.github.naz013.appwidgets.events.UiReminderWidgetListAdapter
import com.github.naz013.appwidgets.singlenote.SingleNoteWidgetConfigViewModel
import com.github.naz013.appwidgets.singlenote.adapter.RecyclableUiNoteWidgetAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteImagesAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteListSelectableAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteWidgetAdapter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appWidgetsModule = module {
  single { WidgetPrefsHolder(get()) }

  factory { WidgetDataProvider(get(), get(), get(), get(), get()) }

  factory { UiBirthdayWidgetListAdapter(get(), get()) }
  factory { UiReminderWidgetListAdapter(get()) }

  factory { AppWidgetUpdaterImpl(get(), get()) as AppWidgetUpdater }
  factory { AppWidgetPreviewUpdaterImpl(get()) as AppWidgetPreviewUpdater }

  factory { UiNoteListSelectableAdapter(get(), get(), get(), get()) }
  factory { UiNoteImagesAdapter() }
  factory { RecyclableUiNoteWidgetAdapter(get(), get(), get(), get(), get()) }
  factory { UiNoteWidgetAdapter(get(), get(), get(), get(), get()) }

  viewModel { SingleNoteWidgetConfigViewModel(get(), get(), get(), get(), get(), get()) }

  factory { (prefs: EventsWidgetPrefsProvider) ->
    EventsAppWidgetViewModel(prefs, get(), get(), get(), get(), get(), get())
  }
}

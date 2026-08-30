package com.github.naz013.appwidgets

import android.content.Context
import com.github.naz013.appwidgets.birthdays.BirthdaysAppWidgetViewModel
import com.github.naz013.appwidgets.birthdays.BirthdaysWidgetConfigViewModel
import com.github.naz013.appwidgets.birthdays.BirthdaysWidgetPrefsProvider
import com.github.naz013.appwidgets.birthdays.UiBirthdayWidgetListAdapter
import com.github.naz013.appwidgets.calendar.CalendarAppWidgetViewModel
import com.github.naz013.appwidgets.calendar.CalendarWidgetConfigViewModel
import com.github.naz013.appwidgets.calendar.CalendarWidgetPrefsProvider
import com.github.naz013.appwidgets.calendar.WidgetDataProvider
import com.github.naz013.appwidgets.combinedbuttons.CombinedWidgetConfigViewModel
import com.github.naz013.appwidgets.combinedbuttons.CombinedWidgetPrefsProvider
import com.github.naz013.appwidgets.compose.ComposeResourceProvider
import com.github.naz013.appwidgets.events.EventsAppWidgetViewModel
import com.github.naz013.appwidgets.events.EventsWidgetConfigViewModel
import com.github.naz013.appwidgets.events.EventsWidgetPrefsProvider
import com.github.naz013.appwidgets.events.UiReminderWidgetListAdapter
import com.github.naz013.appwidgets.googletasks.GoogleTasksAppWidgetViewModel
import com.github.naz013.appwidgets.googletasks.GoogleTasksWidgetPrefsProvider
import com.github.naz013.appwidgets.googletasks.TasksWidgetConfigViewModel
import com.github.naz013.appwidgets.notes.NotesAppWidgetViewModel
import com.github.naz013.appwidgets.notes.NotesWidgetConfigViewModel
import com.github.naz013.appwidgets.notes.NotesWidgetPrefsProvider
import com.github.naz013.appwidgets.singlenote.SingleNoteAppWidgetViewModel
import com.github.naz013.appwidgets.singlenote.SingleNoteWidgetConfigViewModel
import com.github.naz013.appwidgets.singlenote.SingleNoteWidgetPrefsProvider
import com.github.naz013.appwidgets.singlenote.adapter.RecyclableUiNoteWidgetAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteWidgetAdapter
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appWidgetsModule = module {
  factoryOf(::WidgetDataProvider)
  factoryOf(::ComposeResourceProvider)

  factory { UiBirthdayWidgetListAdapter(get(), get()) }
  factory { UiReminderWidgetListAdapter(get()) }

  factory { AppWidgetUpdaterImpl(get(), get()) as AppWidgetUpdater }
  factory { AppWidgetPreviewUpdaterImpl(get()) as AppWidgetPreviewUpdater }

  factory { RecyclableUiNoteWidgetAdapter(get(), get(), get(), get(), get()) }
  factory { UiNoteWidgetAdapter(get(), get(), get(), get(), get()) }

  viewModel { (widgetId: Int) ->
    SingleNoteWidgetConfigViewModel(
      get(),
      get(),
      SingleNoteWidgetPrefsProvider(get(), widgetId),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }

  viewModel { (widgetId: Int) ->
    NotesWidgetConfigViewModel(NotesWidgetPrefsProvider(get(), widgetId), get(), get(), get(), get())
  }

  viewModel { (widgetId: Int) ->
    CalendarWidgetConfigViewModel(CalendarWidgetPrefsProvider(get(), widgetId), get(), get(), get(), get())
  }

  viewModel { (widgetId: Int) ->
    BirthdaysWidgetConfigViewModel(BirthdaysWidgetPrefsProvider(get(), widgetId), get(), get(), get(), get())
  }

  viewModel { (widgetId: Int) ->
    CombinedWidgetConfigViewModel(get(), get(), CombinedWidgetPrefsProvider(get(), widgetId), get(), get())
  }

  viewModel { (widgetId: Int) ->
    TasksWidgetConfigViewModel(
      GoogleTasksWidgetPrefsProvider(get(), widgetId),
      get(),
      get(),
      get(),
      get()
    )
  }

  factory { (prefs: EventsWidgetPrefsProvider) ->
    EventsAppWidgetViewModel(prefs, get(), get(), get(), get(), get(), get())
  }

  factory { (prefs: NotesWidgetPrefsProvider) ->
    NotesAppWidgetViewModel(prefs, get(), get(), get())
  }

  factory { (prefs: BirthdaysWidgetPrefsProvider) ->
    BirthdaysAppWidgetViewModel(prefs, get(), get())
  }

  factory { (prefs: GoogleTasksWidgetPrefsProvider) ->
    GoogleTasksAppWidgetViewModel(prefs, get(), get(), get())
  }

  factory { (context: Context, prefs: CalendarWidgetPrefsProvider) ->
    CalendarAppWidgetViewModel(context, prefs, get(), get(), get(), get())
  }

  factory { (context: Context, prefs: SingleNoteWidgetPrefsProvider) ->
    SingleNoteAppWidgetViewModel(context, prefs, get(), get())
  }

  viewModel { (widgetId: Int) ->
    EventsWidgetConfigViewModel(EventsWidgetPrefsProvider(get(), widgetId), get(), get(), get(), get())
  }
}

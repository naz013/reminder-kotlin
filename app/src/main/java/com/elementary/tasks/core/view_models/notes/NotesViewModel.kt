package com.elementary.tasks.core.view_models.notes

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.view_models.DispatcherProvider

class NotesViewModel(
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider
) : BaseNotesViewModel(appDb, prefs, calendarUtils, eventControlFactory, dispatcherProvider) {
  val notes = appDb.notesDao().loadAll()
}

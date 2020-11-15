package com.elementary.tasks.core.view_models.notes

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Prefs

class NotesViewModel(
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory
) : BaseNotesViewModel(appDb, prefs, calendarUtils, eventControlFactory) {
  val notes = appDb.notesDao().loadAll()
}

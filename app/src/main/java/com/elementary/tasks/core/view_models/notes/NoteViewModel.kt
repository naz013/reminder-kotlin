package com.elementary.tasks.core.view_models.notes

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault

class NoteViewModel(
  key: String,
  appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory
) : BaseNotesViewModel(appDb, prefs, calendarUtils, eventControlFactory) {

  val note = appDb.notesDao().loadById(key)
  val reminder = appDb.reminderDao().loadByNoteKey(if (key == "") "1" else key)

  var hasSameInDb: Boolean = false

  fun findSame(id: String) {
    launchDefault {
      val note = appDb.notesDao().getById(id)
      hasSameInDb = note?.note != null
    }
  }
}

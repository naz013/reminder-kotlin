package com.elementary.tasks.core.view_models.notes

class NotesViewModel : BaseNotesViewModel() {
  val notes = appDb.notesDao().loadAll()
}

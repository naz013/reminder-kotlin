package com.elementary.tasks.reminder.build.formatter.`object`

import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.reminder.build.formatter.Formatter

class NoteFormatter : Formatter<UiNoteList>() {

  override fun format(note: UiNoteList): String {
    return note.text
  }
}

package com.elementary.tasks.reminder.build.formatter.`object`

import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.github.naz013.ui.notification.settings.Formatter

class NoteFormatter : Formatter<UiNoteList>() {
  override fun format(note: UiNoteList): String = note.text
}

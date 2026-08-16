package com.github.naz013.feature.reminder.build.formatter.`object`

import com.github.naz013.feature.reminder.note.UiNoteList
import com.github.naz013.ui.notification.settings.Formatter

class NoteFormatter : Formatter<UiNoteList>() {
  override fun format(note: UiNoteList): String = note.text
}

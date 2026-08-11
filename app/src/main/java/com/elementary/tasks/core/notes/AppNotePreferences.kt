package com.elementary.tasks.core.notes

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.ui.note.NotePreferences

/**
 * `ui-note`/`feature-note` can't depend on `app`, so this wraps the note-related subset of app's
 * monolithic `Prefs` SharedPreferences store behind [NotePreferences] instead.
 */
class AppNotePreferences(
  private val prefs: Prefs,
) : NotePreferences {
  override val is24HourFormat: Boolean
    get() = prefs.is24HourFormat
  override val hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled
  override val isNoteFontSizeRememberingEnabled: Boolean
    get() = prefs.isNoteFontSizeRememberingEnabled
  override val isNoteFontStyleRememberingEnabled: Boolean
    get() = prefs.isNoteFontStyleRememberingEnabled
  override val isNoteColorRememberingEnabled: Boolean
    get() = prefs.isNoteColorRememberingEnabled
  override val notePalette: Int
    get() = prefs.notePalette

  override var isNotesGridEnabled: Boolean
    get() = prefs.isNotesGridEnabled
    set(value) { prefs.isNotesGridEnabled = value }
  override var noteOrder: String
    get() = prefs.noteOrder
    set(value) { prefs.noteOrder = value }
  override var lastNoteFontSize: Int
    get() = prefs.lastNoteFontSize
    set(value) { prefs.lastNoteFontSize = value }
  override var lastNoteFontStyle: Int
    get() = prefs.lastNoteFontStyle
    set(value) { prefs.lastNoteFontStyle = value }
  override var lastNoteTitleFontSize: Int
    get() = prefs.lastNoteTitleFontSize
    set(value) { prefs.lastNoteTitleFontSize = value }
  override var lastNoteTitleFontStyle: Int
    get() = prefs.lastNoteTitleFontStyle
    set(value) { prefs.lastNoteTitleFontStyle = value }
  override var lastNoteColor: Int
    get() = prefs.lastNoteColor
    set(value) { prefs.lastNoteColor = value }
  override var noteColorOpacity: Int
    get() = prefs.noteColorOpacity
    set(value) { prefs.noteColorOpacity = value }
}

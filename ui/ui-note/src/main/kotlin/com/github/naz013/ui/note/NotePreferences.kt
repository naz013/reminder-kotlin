package com.github.naz013.ui.note

/**
 * Seam over the note-related subset of app's monolithic `Prefs` SharedPreferences wrapper, which
 * lives in `app` and can't be depended on from `ui-note`/`feature-note`. Implemented in `app` by
 * wrapping `Prefs` and bound via Koin there - see `AppNotePreferences`.
 */
interface NotePreferences {
  val is24HourFormat: Boolean
  val hapticsEnabled: Boolean
  val isNoteFontSizeRememberingEnabled: Boolean
  val isNoteFontStyleRememberingEnabled: Boolean
  val isNoteColorRememberingEnabled: Boolean

  var notesLayoutMode: ListLayoutMode
  var noteOrder: String
  var lastNoteFontSize: Int
  var lastNoteFontStyle: Int
  var lastNoteColor: Int
  var noteColorOpacity: Int
}

package com.github.naz013.feature.settings

interface NoteSettingsPreferences {
  var isNoteColorRememberingEnabled: Boolean
  var isNoteFontSizeRememberingEnabled: Boolean
  var isNoteFontStyleRememberingEnabled: Boolean
  var isNoteTextColorRememberingEnabled: Boolean
  var noteColorOpacity: Int
  val hapticsEnabled: Boolean
}

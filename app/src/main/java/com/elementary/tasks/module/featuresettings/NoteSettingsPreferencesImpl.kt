package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.settings.NoteSettingsPreferences

class NoteSettingsPreferencesImpl(
  private val prefs: Prefs,
) : NoteSettingsPreferences {
  override var isNoteColorRememberingEnabled: Boolean
    get() = prefs.isNoteColorRememberingEnabled
    set(value) { prefs.isNoteColorRememberingEnabled = value }

  override var isNoteFontSizeRememberingEnabled: Boolean
    get() = prefs.isNoteFontSizeRememberingEnabled
    set(value) { prefs.isNoteFontSizeRememberingEnabled = value }

  override var isNoteFontStyleRememberingEnabled: Boolean
    get() = prefs.isNoteFontStyleRememberingEnabled
    set(value) { prefs.isNoteFontStyleRememberingEnabled = value }

  override var noteColorOpacity: Int
    get() = prefs.noteColorOpacity
    set(value) { prefs.noteColorOpacity = value }

  override val hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled
}

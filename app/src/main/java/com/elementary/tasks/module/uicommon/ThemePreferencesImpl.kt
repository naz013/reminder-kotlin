package com.elementary.tasks.module.uicommon

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.ui.common.theme.ThemePreferences

class ThemePreferencesImpl(
  private val prefs: Prefs,
) : ThemePreferences {
  override val nightMode: Int
    get() {
      return prefs.nightMode
    }

  override val mapStyle: Int
    get() {
      return prefs.mapStyle
    }

  override val birthdayColor: Int
    get() {
      return prefs.birthdayColor
    }

  override val notePalette: Int
    get() {
      return prefs.notePalette
    }
}

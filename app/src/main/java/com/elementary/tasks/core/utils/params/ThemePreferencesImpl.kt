package com.elementary.tasks.core.utils.params

import com.github.naz013.ui.common.theme.ThemePreferences

class ThemePreferencesImpl(
  private val prefs: Prefs
) : ThemePreferences {
  override val nightMode: Int
    get() {
      return prefs.nightMode
    }

  override val mapStyle: Int
    get() {
      return prefs.mapStyle
    }

  override val useDynamicColors: Boolean
    get() {
      return prefs.useDynamicColors
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

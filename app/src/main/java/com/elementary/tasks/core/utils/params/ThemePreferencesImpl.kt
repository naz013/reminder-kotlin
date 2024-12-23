package com.elementary.tasks.core.utils.params

import com.github.naz013.ui.common.theme.ThemePreferences

class ThemePreferencesImpl(
  private val prefs: Prefs
) : ThemePreferences {
  override val nightMode: Int
    get() = prefs.nightMode

  override val mapStyle: Int
    get() = prefs.mapStyle

  override val useDynamicColors: Boolean
    get() = prefs.useDynamicColors

  override val birthdayColor: Int
    get() = prefs.birthdayColor

  override val notePalette: Int
    get() = prefs.notePalette
}

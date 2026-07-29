package com.elementary.tasks.module.appwidgets

import androidx.compose.ui.graphics.Color
import com.github.naz013.appwidgets.singlenote.NoteWidgetPreferences
import com.github.naz013.ui.common.theme.ThemeProvider

class NoteWidgetPreferencesImpl(
  private val themeProvider: ThemeProvider
) : NoteWidgetPreferences {

  override fun getNoteColors(): List<Color> {
    return themeProvider.noteWidgetSliderColors()
  }
}

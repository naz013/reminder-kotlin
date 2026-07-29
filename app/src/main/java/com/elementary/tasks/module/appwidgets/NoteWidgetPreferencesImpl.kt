package com.elementary.tasks.module.appwidgets

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.notes.NoteColorEngine
import com.github.naz013.appwidgets.singlenote.NoteWidgetPreferences

class NoteWidgetPreferencesImpl(
  private val noteColorEngine: NoteColorEngine
) : NoteWidgetPreferences {

  override fun getNoteColors(): List<Color> {
    return noteColorEngine.allColors()
  }
}

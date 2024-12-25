package com.elementary.tasks.notes.preview

import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.github.naz013.ui.common.theme.ThemeProvider

class ImagesSingleton(
  private val themeProvider: ThemeProvider
) {

  private var images: MutableList<UiNoteImage> = mutableListOf()
  private var backgroundColor: Int = -1

  fun getColor(): Int = backgroundColor

  fun getCurrent(): List<UiNoteImage> = images

  fun clear() {
    backgroundColor = -1
    images.clear()
  }

  fun setCurrent(images: List<UiNoteImage>, color: Int, palette: Int) {
    clear()
    this.backgroundColor = getNoteColor(color, palette)
    this.images.addAll(images)
  }

  fun setCurrent(images: List<UiNoteImage>, backgroundColor: Int) {
    clear()
    this.backgroundColor = backgroundColor
    this.images.addAll(images)
  }

  private fun getNoteColor(color: Int, palette: Int): Int {
    if (color == -1 || palette == -1) {
      return -1
    }
    return themeProvider.getNoteLightColor(color, 100, palette)
  }
}

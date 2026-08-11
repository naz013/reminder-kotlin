package com.github.naz013.feature.note.preview

import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.note.UiNoteImage

class ImagesSingleton {
  private var images: MutableList<UiNoteImage> = mutableListOf()
  private var color: Color = Color.Unspecified

  fun getColor(): Color = color

  fun getCurrent(): List<UiNoteImage> = images

  fun clear() {
    color = Color.Unspecified
    images.clear()
  }

  fun setCurrent(
    images: List<UiNoteImage>,
    backgroundColor: Color,
  ) {
    clear()
    this.color = backgroundColor
    this.images.addAll(images)
  }
}

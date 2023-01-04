package com.elementary.tasks.notes.preview

import com.elementary.tasks.core.data.ui.note.UiNoteImage

class ImagesSingleton {

  private var images: MutableList<UiNoteImage> = mutableListOf()

  fun getCurrent(): List<UiNoteImage> = images

  fun clear() {
    images.clear()
  }

  fun setCurrent(images: List<UiNoteImage>) {
    clear()
    this.images.addAll(images)
  }
}

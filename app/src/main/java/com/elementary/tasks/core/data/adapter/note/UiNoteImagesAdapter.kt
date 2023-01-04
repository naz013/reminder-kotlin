package com.elementary.tasks.core.data.adapter.note

import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.ui.note.UiNoteImage

class UiNoteImagesAdapter {

  fun convert(images: List<ImageFile>): List<UiNoteImage> {
    return images.map { convertImage(it) }
  }

  private fun convertImage(imageFile: ImageFile): UiNoteImage {
    return UiNoteImage(
      id = imageFile.id,
      data = imageFile.image
    )
  }
}

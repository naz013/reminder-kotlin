package com.elementary.tasks.core.data.adapter.note

import com.github.naz013.domain.note.ImageFile
import com.elementary.tasks.core.data.ui.note.UiNoteImage

class UiNoteImagesAdapter {

  fun convert(images: List<ImageFile>): List<UiNoteImage> {
    return images.map { convertImage(it) }
  }

  private fun convertImage(imageFile: ImageFile): UiNoteImage {
    return UiNoteImage(
      id = imageFile.id,
      filePath = imageFile.filePath,
      fileName = imageFile.fileName
    )
  }
}

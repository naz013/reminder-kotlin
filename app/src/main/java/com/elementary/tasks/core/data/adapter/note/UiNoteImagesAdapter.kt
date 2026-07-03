package com.elementary.tasks.core.data.adapter.note

import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.github.naz013.domain.note.ImageFile

class UiNoteImagesAdapter {
  fun convert(images: List<ImageFile>): List<UiNoteImage> = images.map { convertImage(it) }

  private fun convertImage(imageFile: ImageFile): UiNoteImage =
    UiNoteImage(
      id = imageFile.id,
      filePath = imageFile.filePath,
      fileName = imageFile.fileName,
    )
}

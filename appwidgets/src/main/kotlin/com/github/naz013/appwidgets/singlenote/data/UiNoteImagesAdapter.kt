package com.github.naz013.appwidgets.singlenote.data

import com.github.naz013.domain.note.ImageFile

internal class UiNoteImagesAdapter {

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

package com.elementary.tasks.core.data.adapter.note

import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteEdit

class UiNoteEditAdapter(
  private val uiNoteImagesAdapter: UiNoteImagesAdapter
) {

  fun convert(noteWithImages: NoteWithImages): UiNoteEdit {
    return UiNoteEdit(
      id = noteWithImages.getKey(),
      typeface = noteWithImages.getStyle(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      colorPosition = noteWithImages.getColor(),
      colorPalette = noteWithImages.getPalette(),
      opacity = noteWithImages.getOpacity()
    )
  }
}

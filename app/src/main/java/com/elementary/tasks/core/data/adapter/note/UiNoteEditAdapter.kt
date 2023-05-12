package com.elementary.tasks.core.data.adapter.note

import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteEdit
import com.elementary.tasks.core.utils.params.Prefs

class UiNoteEditAdapter(
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
  private val prefs: Prefs
) {

  fun convert(noteWithImages: NoteWithImages): UiNoteEdit {
    val textSize = if (noteWithImages.getFontSize() == -1) {
      prefs.noteTextSize + 12
    } else {
      noteWithImages.getFontSize()
    }
    return UiNoteEdit(
      id = noteWithImages.getKey(),
      typeface = noteWithImages.getStyle(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      colorPosition = noteWithImages.getColor(),
      colorPalette = noteWithImages.getPalette(),
      opacity = noteWithImages.getOpacity(),
      fontSize = textSize
    )
  }
}

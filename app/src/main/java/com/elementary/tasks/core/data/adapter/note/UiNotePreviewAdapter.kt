package com.elementary.tasks.core.data.adapter.note

import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNotePreview
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.params.Prefs

class UiNotePreviewAdapter(
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
  private val prefs: Prefs
) {

  fun convert(noteWithImages: NoteWithImages): UiNotePreview {
    val backgroundColor = themeProvider.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )

    val textSize = if (noteWithImages.getFontSize() == -1) {
      prefs.noteTextSize + 12
    } else {
      noteWithImages.getFontSize()
    }

    return UiNotePreview(
      id = noteWithImages.getKey(),
      backgroundColor = backgroundColor,
      typeface = AssetsUtil.getTypeface(contextProvider.context, noteWithImages.getStyle()),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      uniqueId = noteWithImages.note?.uniqueId ?: 1133,
      opacity = noteWithImages.getOpacity(),
      textSize = textSize.toFloat()
    )
  }
}

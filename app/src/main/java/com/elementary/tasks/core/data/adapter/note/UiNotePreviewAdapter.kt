package com.elementary.tasks.core.data.adapter.note

import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNotePreview
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.io.AssetsUtil

class UiNotePreviewAdapter(
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter
) {

  fun convert(noteWithImages: NoteWithImages): UiNotePreview {
    val backgroundColor = themeProvider.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )

    return UiNotePreview(
      id = noteWithImages.getKey(),
      backgroundColor = backgroundColor,
      typeface = AssetsUtil.getTypeface(contextProvider.context, noteWithImages.getStyle()),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      uniqueId = noteWithImages.note?.uniqueId ?: 1133,
      opacity = noteWithImages.getOpacity()
    )
  }
}

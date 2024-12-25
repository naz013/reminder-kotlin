package com.elementary.tasks.core.data.adapter.note

import com.elementary.tasks.core.data.ui.note.UiNotePreview
import com.github.naz013.ui.common.theme.ThemeProvider
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.common.ContextProvider

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

    val textSize = if (noteWithImages.getFontSize() == -1) {
      FontParams.DEFAULT_FONT_SIZE
    } else {
      noteWithImages.getFontSize()
    }

    return UiNotePreview(
      id = noteWithImages.getKey(),
      backgroundColor = backgroundColor,
      typeface = AssetsUtil.getTypeface(contextProvider.themedContext, noteWithImages.getStyle()),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary(),
      uniqueId = noteWithImages.note?.uniqueId ?: 1133,
      opacity = noteWithImages.getOpacity(),
      textSize = textSize.toFloat(),
      isArchived = noteWithImages.note?.archived ?: false
    )
  }
}

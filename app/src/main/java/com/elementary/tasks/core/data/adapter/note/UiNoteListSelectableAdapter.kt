package com.elementary.tasks.core.data.adapter.note

import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteListSelectable
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.ui.font.FontParams

class UiNoteListSelectableAdapter(
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter
) {

  fun convert(noteWithImages: NoteWithImages): UiNoteListSelectable {
    val backgroundColor = themeProvider.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )

    val isDarkIcon = if (noteWithImages.getOpacity().isAlmostTransparent()) {
      themeProvider.isDark
    } else {
      backgroundColor.isColorDark()
    }

    val isDarkBg = (noteWithImages.getOpacity().isAlmostTransparent() && themeProvider.isDark) ||
      backgroundColor.isColorDark()
    val textColor = if (isDarkBg) {
      ContextCompat.getColor(contextProvider.context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(contextProvider.context, R.color.pureBlack)
    }

    val textSize = if (noteWithImages.getFontSize() == -1) {
      FontParams.DEFAULT_FONT_SIZE
    } else {
      noteWithImages.getFontSize()
    }

    return UiNoteListSelectable(
      id = noteWithImages.getKey(),
      backgroundColor = backgroundColor,
      dartIcon = isDarkIcon,
      textColor = textColor,
      typeface = AssetsUtil.getTypeface(contextProvider.context, noteWithImages.getStyle()),
      fontSize = textSize.toFloat(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary()
    )
  }
}

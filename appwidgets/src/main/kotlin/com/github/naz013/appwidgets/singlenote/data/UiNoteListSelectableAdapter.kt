package com.github.naz013.appwidgets.singlenote.data

import androidx.core.content.ContextCompat
import com.github.naz013.appwidgets.R
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.ui.common.font.FontApi
import com.github.naz013.ui.common.isAlmostTransparent
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider

internal class UiNoteListSelectableAdapter(
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
  private val fontApi: FontApi
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
      ContextCompat.getColor(contextProvider.themedContext, R.color.pureWhite)
    } else {
      ContextCompat.getColor(contextProvider.themedContext, R.color.pureBlack)
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
      typeface = fontApi.getTypeface(noteWithImages.getStyle()),
      fontSize = textSize.toFloat(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary()
    )
  }
}

package com.elementary.tasks.core.data.adapter.note

import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.github.naz013.domain.note.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteListSelectable
import com.github.naz013.feature.common.android.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.feature.common.android.isAlmostTransparent
import com.github.naz013.feature.common.android.isColorDark
import com.github.naz013.domain.font.FontParams

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
      typeface = AssetsUtil.getTypeface(contextProvider.themedContext, noteWithImages.getStyle()),
      fontSize = textSize.toFloat(),
      images = uiNoteImagesAdapter.convert(noteWithImages.images),
      text = noteWithImages.getSummary()
    )
  }
}

package com.github.naz013.appwidgets.notes

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.github.naz013.appwidgets.notes.data.NotesAppWidgetState
import com.github.naz013.appwidgets.notes.data.UiNoteWidgetItem
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository
import com.github.naz013.ui.common.isAlmostTransparent
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.note.NotePreferences

internal class NotesAppWidgetViewModel(
  private val prefsProvider: NotesWidgetPrefsProvider,
  private val noteRepository: NoteRepository,
  private val themeProvider: ThemeProvider,
  private val notePreferences: NotePreferences
) {

  suspend fun getState(): NotesAppWidgetState {
    val headerBackgroundColor = prefsProvider.getBackground()
    return NotesAppWidgetState(
      widgetId = prefsProvider.widgetId,
      backgroundColor = headerBackgroundColor,
      items = noteRepository.getNotes(
        isArchived = false,
        query = "",
        sortOrder = notePreferences.noteOrder
      ).map { it.toUiNoteWidgetItem() }
    )
  }

  private fun NoteWithImages.toUiNoteWidgetItem(): UiNoteWidgetItem {
    val textSize = if (getFontSize() == -1) {
      FontParams.DEFAULT_FONT_SIZE
    } else {
      getFontSize()
    }
    val contentColor = if (getOpacity().isAlmostTransparent()) Color.White else Color.Black
    val imageBytes = images.firstOrNull()?.image
    val image = imageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    return UiNoteWidgetItem(
      uuId = getKey(),
      text = getSummary(),
      textSize = textSize.sp,
      backgroundColor = Color(themeProvider.getNoteLightColor(getColor(), getOpacity())),
      contentColor = contentColor,
      image = image
    )
  }
}

package com.elementary.tasks.reminder.preview

import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.note.UiNoteListAdapter
import com.elementary.tasks.core.data.ui.UiTextElement
import com.github.naz013.ui.common.theme.ColorProvider
import com.github.naz013.ui.common.UnitsConverter
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewData
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewHeader
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewNote
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.common.TextProvider

class NoteToUiReminderPreviewNote(
  private val textProvider: TextProvider,
  private val colorProvider: ColorProvider,
  private val unitsConverter: UnitsConverter,
  private val uiNoteListAdapter: UiNoteListAdapter
) {

  operator fun invoke(
    noteWithImages: NoteWithImages
  ): List<UiReminderPreviewData> {
    return listOf(
      UiReminderPreviewHeader(
        UiTextElement(
          text = textProvider.getText(R.string.note),
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(18f),
            textStyle = UiTextStyle.BOLD,
            textColor = colorProvider.getColorOnBackground()
          )
        )
      ),
      UiReminderPreviewNote(uiNoteListAdapter.convert(noteWithImages))
    )
  }
}

package com.elementary.tasks.reminder.preview

import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.ui.UiTextElement
import com.github.naz013.ui.common.theme.ColorProvider
import com.github.naz013.ui.common.UnitsConverter
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewData
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewGoogleTask
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewHeader
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.common.TextProvider

class GoogleTaskToUiReminderPreviewGoogleTask(
  private val textProvider: TextProvider,
  private val colorProvider: ColorProvider,
  private val unitsConverter: UnitsConverter,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter
) {

  operator fun invoke(
    googleTask: GoogleTask,
    googleTaskList: GoogleTaskList?
  ): List<UiReminderPreviewData> {
    return listOf(
      UiReminderPreviewHeader(
        UiTextElement(
          text = textProvider.getText(R.string.google_task),
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(18f),
            textStyle = UiTextStyle.BOLD,
            textColor = colorProvider.getColorOnBackground()
          )
        )
      ),
      UiReminderPreviewGoogleTask(uiGoogleTaskListAdapter.convert(googleTask, googleTaskList))
    )
  }
}

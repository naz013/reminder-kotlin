package com.elementary.tasks.reminder.preview

import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.os.ColorProvider
import com.elementary.tasks.core.os.UnitsConverter
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewData
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewGoogleTask
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewHeader

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

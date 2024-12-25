package com.elementary.tasks.reminder.preview

import android.net.Uri
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiTextElement
import com.github.naz013.ui.common.theme.ColorProvider
import com.github.naz013.ui.common.UnitsConverter
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewAttachment
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewData
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewHeader
import com.github.naz013.common.TextProvider

class AttachmentToUiReminderPreviewAttachment(
  private val textProvider: TextProvider,
  private val colorProvider: ColorProvider,
  private val unitsConverter: UnitsConverter,
  private val attachmentFileAdapter: UriToAttachmentFileAdapter
) {

  operator fun invoke(
    attachments: List<String>
  ): List<UiReminderPreviewData> {
    val data = attachments.map { Uri.parse(it) }.map {
      val attachment = attachmentFileAdapter(it)
      UiReminderPreviewAttachment(
        file = attachment,
        text = UiTextElement(
          text = attachment.name,
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(16f),
            textStyle = UiTextStyle.NORMAL,
            textColor = colorProvider.getColorOnBackground()
          )
        )
      )
    }

    return listOf(
      UiReminderPreviewHeader(
        UiTextElement(
          text = textProvider.getText(R.string.builder_attachments),
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(18f),
            textStyle = UiTextStyle.BOLD,
            textColor = colorProvider.getColorOnBackground()
          )
        )
      )
    ) + data
  }
}

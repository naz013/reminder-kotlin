package com.elementary.tasks.reminder.build.formatter

import com.github.naz013.ui.notification.settings.Formatter
import android.content.Context
import com.elementary.tasks.R

class AttachmentsFormatter(
  private val context: Context,
) : Formatter<List<String>>() {
  override fun format(attachments: List<String>): String =
    if (attachments.isEmpty()) {
      context.getString(R.string.builder_no_attachments)
    } else {
      context.resources.getQuantityString(
        R.plurals.x_attachments,
        attachments.size,
        attachments.size,
      )
    }
}

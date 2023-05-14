package com.elementary.tasks.core.data.ui.reminder.widget

import com.elementary.tasks.core.data.ui.widget.DateSorted

data class UiReminderWidgetList(
  val uuId: String,
  val text: String = "",
  val dateTime: String = "",
  val remainingTimeFormatted: String = "",
  override val millis: Long
) : DateSorted

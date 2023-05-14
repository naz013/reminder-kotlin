package com.elementary.tasks.core.data.ui.reminder.widget

import com.elementary.tasks.core.data.ui.widget.DateSorted

data class UiReminderWidgetShopList(
  val uuId: String,
  val text: String = "",
  val dateTime: String?,
  val items: List<UiShopListWidget>,
  override val millis: Long
) : DateSorted

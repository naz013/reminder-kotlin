package com.github.naz013.appwidgets.events.data

internal data class UiReminderWidgetList(
  val uuId: String,
  val text: String = "",
  val dateTime: String = "",
  override val millis: Long
) : DateSorted

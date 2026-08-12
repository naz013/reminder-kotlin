package com.github.naz013.appwidgets.events.data

internal data class UiReminderWidgetShopList(
  override val uuId: String,
  val text: String = "",
  val dateTime: String?,
  val items: List<UiShopListWidget>,
  override val millis: Long
) : DateSorted

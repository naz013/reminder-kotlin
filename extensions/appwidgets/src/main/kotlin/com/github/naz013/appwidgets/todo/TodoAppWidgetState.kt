package com.github.naz013.appwidgets.todo

import com.github.naz013.appwidgets.events.data.UiShopListWidget

internal data class TodoAppWidgetState(
  val widgetId: Int,
  val backgroundColor: Int = 0,
  val reminderId: String? = null,
  val title: String = "",
  val items: List<UiShopListWidget> = emptyList()
)

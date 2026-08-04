package com.github.naz013.appwidgets.birthdays

internal data class BirthdaysAppWidgetState(
  val widgetId: Int,
  val backgroundColor: Int,
  val items: List<UiBirthdayWidgetList>
)

package com.github.naz013.appwidgets.googletasks.data

internal data class GoogleTasksAppWidgetState(
  val widgetId: Int,
  val backgroundColor: Int,
  val items: List<UiGoogleTaskWidgetItem>
)

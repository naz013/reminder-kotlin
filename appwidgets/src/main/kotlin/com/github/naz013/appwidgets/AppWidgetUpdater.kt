package com.github.naz013.appwidgets

interface AppWidgetUpdater {
  fun updateAllWidgets()
  fun updateNotesWidget(widgetId: Int = WidgetId.NO_ID)
  fun updateCalendarWidget(widgetId: Int = WidgetId.NO_ID)
  fun updateScheduleWidget(widgetId: Int = WidgetId.NO_ID)
  fun updateBirthdaysWidget(widgetId: Int = WidgetId.NO_ID)
  suspend fun updateEventsWidget(widgetId: Int = WidgetId.NO_ID)
  suspend fun updateCombinedButtonsWidget(widgetId: Int = WidgetId.NO_ID)
}

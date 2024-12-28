package com.github.naz013.appwidgets

interface AppWidgetUpdater {
  fun updateAllWidgets()
  fun updateNotesWidget()
  fun updateCalendarWidget()
  fun updateScheduleWidget()
  fun updateBirthdaysWidget()
  suspend fun updateEventsWidget(widgetId: Int = WidgetId.NO_ID)
}

package com.github.naz013.appwidgets

interface AppWidgetUpdater {
  fun updateAllWidgets()
  fun updateNotesWidget(widgetId: Int = -1)
  fun updateCalendarWidget(widgetId: Int = -1)
  fun updateScheduleWidget(widgetId: Int = -1)
  fun updateBirthdaysWidget(widgetId: Int = -1)
  suspend fun updateEventsWidget(widgetId: Int = -1)
  suspend fun updateCombinedButtonsWidget(widgetId: Int = -1)
  suspend fun updateSingleNoteWidget(widgetId: Int = -1)
}

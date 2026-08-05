package com.github.naz013.appwidgets

interface AppWidgetPreviewUpdater {
  suspend fun updateEventsWidgetPreview()
  suspend fun updateCombinedButtonsWidgetPreview()
  suspend fun updateNotesWidgetPreview()
  suspend fun updateSingleNoteWidgetPreview()
  suspend fun updateCalendarWidgetPreview()
  suspend fun updateScheduleWidgetPreview()
  suspend fun updateBirthdaysWidgetPreview()
}

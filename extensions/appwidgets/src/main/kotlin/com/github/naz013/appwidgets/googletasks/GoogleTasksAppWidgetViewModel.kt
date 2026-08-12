package com.github.naz013.appwidgets.googletasks

import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.googletasks.data.GoogleTasksAppWidgetState
import com.github.naz013.appwidgets.googletasks.data.UiGoogleTaskWidgetItem
import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.ui.common.theme.ThemeProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal class GoogleTasksAppWidgetViewModel(
  private val prefsProvider: GoogleTasksWidgetPrefsProvider,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val themeProvider: ThemeProvider
) {

  suspend fun getState(): GoogleTasksAppWidgetState {
    val listColors = googleTaskListRepository.getAll().associate { it.listId to it.color }
    return GoogleTasksAppWidgetState(
      widgetId = prefsProvider.widgetId,
      backgroundColor = prefsProvider.getBackground(),
      items = googleTaskRepository.getAll().map { it.toUiGoogleTaskWidgetItem(listColors) }
    )
  }

  private fun GoogleTask.toUiGoogleTaskWidgetItem(
    listColors: Map<String, Int>
  ): UiGoogleTaskWidgetItem {
    val listColorCode = listColors[listId] ?: 0
    val iconRes = if (status == GoogleTask.TASKS_COMPLETE) {
      R.drawable.ic_builder_google_task_list
    } else {
      R.drawable.ic_fluent_radio_button
    }
    val dateText = if (dueDate != 0L) {
      val calendar = Calendar.getInstance().apply { timeInMillis = dueDate }
      DATE_FORMAT.format(calendar.time)
    } else {
      null
    }
    return UiGoogleTaskWidgetItem(
      taskId = taskId,
      title = title,
      note = notes.takeIf { it.isNotBlank() },
      dateText = dateText,
      iconRes = iconRes,
      iconTintColor = themeProvider.themedColor(listColorCode)
    )
  }

  companion object {
    private val DATE_FORMAT = SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault())
  }
}

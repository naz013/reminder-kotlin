package com.elementary.tasks.core.data.adapter.google

import androidx.annotation.ColorInt
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.ui.common.theme.ThemeProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UiGoogleTaskListAdapter(
  private val contextProvider: ContextProvider,
) {
  private val full24Format = SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault())

  fun convert(
    googleTask: GoogleTask,
    googleTaskList: GoogleTaskList?,
  ): UiGoogleTaskList =
    UiGoogleTaskList(
      id = googleTask.taskId,
      text = googleTask.title,
      notes = googleTask.notes,
      dueDate = getDue(googleTask.dueDate),
      isCompleted = googleTask.status == GoogleTask.TASKS_COMPLETE,
      taskListColor = getColor(googleTaskList),
      reminderId = googleTask.uuId,
    )

  private fun getDue(due: Long): String? =
    if (due != 0L) {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = due
      full24Format.format(calendar.time)
    } else {
      null
    }

  @ColorInt
  private fun getColor(googleTaskList: GoogleTaskList?): Int =
    if (googleTaskList != null) {
      ThemeProvider.themedColor(contextProvider.themedContext, googleTaskList.color)
    } else {
      ThemeProvider.themedColor(contextProvider.themedContext, 0)
    }
}

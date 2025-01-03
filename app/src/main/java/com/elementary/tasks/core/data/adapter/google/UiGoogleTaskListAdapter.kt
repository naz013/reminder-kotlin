package com.elementary.tasks.core.data.adapter.google

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.ViewUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UiGoogleTaskListAdapter(
  private val contextProvider: ContextProvider
) {

  private val full24Format = SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault())

  fun convert(googleTask: GoogleTask, googleTaskList: GoogleTaskList?): UiGoogleTaskList {
    return UiGoogleTaskList(
      id = googleTask.taskId,
      text = googleTask.title,
      notes = googleTask.notes,
      dueDate = getDue(googleTask.dueDate),
      statusIcon = createIcon(
        isChecked = googleTask.status == GoogleTask.TASKS_COMPLETE,
        color = getColor(googleTaskList)
      ),
      taskListColor = getColor(googleTaskList),
      reminderId = googleTask.uuId
    )
  }

  private fun getDue(due: Long): String? {
    return if (due != 0L) {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = due
      full24Format.format(calendar.time)
    } else {
      null
    }
  }

  @ColorInt
  private fun getColor(googleTaskList: GoogleTaskList?): Int {
    return if (googleTaskList != null) {
      ThemeProvider.themedColor(contextProvider.themedContext, googleTaskList.color)
    } else {
      ThemeProvider.themedColor(contextProvider.themedContext, 0)
    }
  }

  private fun createIcon(isChecked: Boolean, color: Int): Bitmap? {
    return if (isChecked) {
      ViewUtils.createIcon(
        context = contextProvider.themedContext,
        res = R.drawable.ic_builder_google_task_list,
        color = color
      )
    } else {
      ViewUtils.createIcon(contextProvider.themedContext, R.drawable.ic_fluent_radio_button, color)
    }
  }
}

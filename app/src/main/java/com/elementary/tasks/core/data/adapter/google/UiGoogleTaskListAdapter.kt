package com.elementary.tasks.core.data.adapter.google

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.ui.ViewUtils
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
      statusIcon = createIcon(googleTask.status == GTasks.TASKS_COMPLETE, getColor(googleTaskList)),
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
      ThemeProvider.themedColor(contextProvider.context, googleTaskList.color)
    } else {
      ThemeProvider.themedColor(contextProvider.context, 0)
    }
  }

  private fun createIcon(isChecked: Boolean, color: Int): Bitmap? {
    return if (isChecked) {
      ViewUtils.createIcon(contextProvider.context, R.drawable.ic_check, color)
    } else {
      ViewUtils.createIcon(contextProvider.context, R.drawable.ic_empty_circle, color)
    }
  }
}

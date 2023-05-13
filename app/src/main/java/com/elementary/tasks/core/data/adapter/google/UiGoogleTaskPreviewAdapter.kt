package com.elementary.tasks.core.data.adapter.google

import androidx.annotation.ColorInt
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskPreview
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager

class UiGoogleTaskPreviewAdapter(
  private val contextProvider: ContextProvider,
  private val dateTimeManager: DateTimeManager
) {

  fun convert(googleTask: GoogleTask, googleTaskList: GoogleTaskList?): UiGoogleTaskPreview {
    return UiGoogleTaskPreview(
      id = googleTask.taskId,
      text = googleTask.title,
      notes = googleTask.notes.takeIf { it.isNotEmpty() },
      dueDate = googleTask.dueDate.takeIf { it != 0L }?.let { dateTimeManager.getFullDateTime(it) },
      createdDate = googleTask.updateDate.takeIf { it != 0L }
        ?.let { dateTimeManager.getFullDateTime(it) },
      completedDate = googleTask.completeDate.takeIf { it != 0L }
        ?.let { dateTimeManager.getFullDateTime(it) },
      isCompleted = googleTask.status == GTasks.TASKS_COMPLETE,
      taskListName = googleTaskList?.title ?: "",
      taskListColor = getColor(googleTaskList)
    )
  }

  @ColorInt
  private fun getColor(googleTaskList: GoogleTaskList?): Int {
    return if (googleTaskList != null) {
      ThemeProvider.themedColor(contextProvider.context, googleTaskList.color)
    } else {
      ThemeProvider.themedColor(contextProvider.context, 0)
    }
  }
}

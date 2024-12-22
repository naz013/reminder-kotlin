package com.elementary.tasks.core.data.adapter.google

import androidx.annotation.ColorInt
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskPreview
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.android.ContextProvider

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
      isCompleted = googleTask.isCompleted(),
      taskListName = googleTaskList?.title ?: "",
      taskListColor = getColor(googleTaskList)
    )
  }

  @ColorInt
  private fun getColor(googleTaskList: GoogleTaskList?): Int {
    return if (googleTaskList != null) {
      ThemeProvider.themedColor(contextProvider.themedContext, googleTaskList.color)
    } else {
      ThemeProvider.themedColor(contextProvider.themedContext, 0)
    }
  }
}

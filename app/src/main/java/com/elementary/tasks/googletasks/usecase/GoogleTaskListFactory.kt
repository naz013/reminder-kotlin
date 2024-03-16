package com.elementary.tasks.googletasks.usecase

import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.google.api.services.tasks.model.TaskList

class GoogleTaskListFactory(
  private val dateTimeManager: DateTimeManager
) {

  fun create(taskList: TaskList, color: Int): GoogleTaskList {
    return GoogleTaskList(
      color = color,
      title = taskList.title ?: "",
      listId = taskList.id ?: "",
      eTag = taskList.etag ?: "",
      kind = taskList.kind ?: "",
      selfLink = taskList.selfLink ?: "",
      updated = dateTimeManager.fromRfc3339Format(taskList.updated),
      uploaded = true
    )
  }

  fun update(googleTaskList: GoogleTaskList, taskList: TaskList): GoogleTaskList {
    return googleTaskList.copy(
      title = taskList.title ?: "",
      listId = taskList.id ?: "",
      eTag = taskList.etag ?: "",
      kind = taskList.kind ?: "",
      selfLink = taskList.selfLink ?: "",
      updated = dateTimeManager.fromRfc3339Format(taskList.updated),
      uploaded = true
    )
  }
}

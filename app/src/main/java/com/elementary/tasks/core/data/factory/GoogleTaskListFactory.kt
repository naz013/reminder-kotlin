package com.elementary.tasks.core.data.factory

import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.google.api.services.tasks.model.TaskList

class GoogleTaskListFactory(
  private val dateTimeManager: DateTimeManager
) {

  fun create(taskList: TaskList, color: Int): GoogleTaskList {
    val googleTaskList = GoogleTaskList()

    googleTaskList.color = color
    googleTaskList.title = taskList.title ?: ""
    googleTaskList.listId = taskList.id ?: ""
    googleTaskList.eTag = taskList.etag ?: ""
    googleTaskList.kind = taskList.kind ?: ""
    googleTaskList.selfLink = taskList.selfLink ?: ""
    googleTaskList.updated = dateTimeManager.fromRfc3339Format(taskList.updated)

    return googleTaskList
  }

  fun update(googleTaskList: GoogleTaskList, taskList: TaskList): GoogleTaskList {
    googleTaskList.title = taskList.title ?: ""
    googleTaskList.listId = taskList.id ?: ""
    googleTaskList.eTag = taskList.etag ?: ""
    googleTaskList.kind = taskList.kind ?: ""
    googleTaskList.selfLink = taskList.selfLink ?: ""
    googleTaskList.updated = dateTimeManager.fromRfc3339Format(taskList.updated)
    return googleTaskList
  }
}

package com.elementary.tasks.core.data.factory

import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.google.api.services.tasks.model.Task

class GoogleTaskFactory(
  private val dateTimeManager: DateTimeManager
) {

  fun create(task: Task, listId: String): GoogleTask {
    val googleTask = GoogleTask()

    googleTask.listId = listId

    val taskId = task.id ?: ""
    var isDeleted = false
    try {
      isDeleted = task.deleted ?: false
    } catch (ignored: NullPointerException) {
    }

    var isHidden = false
    try {
      isHidden = task.hidden ?: false
    } catch (ignored: NullPointerException) {
    }

    googleTask.selfLink = task.selfLink ?: ""
    googleTask.kind = task.kind ?: ""
    googleTask.eTag = task.etag ?: ""
    googleTask.title = task.title ?: ""
    googleTask.taskId = taskId
    googleTask.completeDate = dateTimeManager.fromRfc3339Format(task.completed)
    googleTask.del = if (isDeleted) 1 else 0
    googleTask.hidden = if (isHidden) 1 else 0
    googleTask.dueDate = dateTimeManager.fromRfc3339Format(task.due)
    googleTask.notes = task.notes ?: ""
    googleTask.parent = task.parent ?: ""
    googleTask.position = task.position ?: ""
    googleTask.updateDate = dateTimeManager.fromRfc3339Format(task.updated)
    googleTask.status = task.status ?: ""

    return googleTask
  }

  fun update(googleTask: GoogleTask, task: Task): GoogleTask {
    var isDeleted = false
    try {
      isDeleted = task.deleted ?: false
    } catch (ignored: NullPointerException) {
    }

    var isHidden = false
    try {
      isHidden = task.hidden ?: false
    } catch (ignored: NullPointerException) {
    }

    googleTask.selfLink = task.selfLink ?: ""
    googleTask.kind = task.kind ?: ""
    googleTask.eTag = task.etag ?: ""
    googleTask.title = task.title ?: ""
    googleTask.taskId = task.id ?: ""
    googleTask.completeDate = dateTimeManager.fromRfc3339Format(task.completed)
    googleTask.del = if (isDeleted) 1 else 0
    googleTask.hidden = if (isHidden) 1 else 0
    googleTask.dueDate = dateTimeManager.fromRfc3339Format(task.due)
    googleTask.notes = task.notes ?: ""
    googleTask.parent = task.parent ?: ""
    googleTask.position = task.position ?: ""
    googleTask.updateDate = dateTimeManager.fromRfc3339Format(task.updated)
    googleTask.status = task.status ?: ""

    return googleTask
  }
}

package com.elementary.tasks.googletasks.usecase

import com.github.naz013.domain.GoogleTask
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.google.api.services.tasks.model.Task

class GoogleTaskFactory(
  private val dateTimeManager: DateTimeManager
) {

  fun create(task: Task, listId: String): GoogleTask {
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

    return GoogleTask(
      listId = listId,
      selfLink = task.selfLink ?: "",
      kind = task.kind ?: "",
      eTag = task.etag ?: "",
      title = task.title ?: "",
      taskId = task.id ?: "",
      completeDate = dateTimeManager.fromRfc3339Format(task.completed),
      del = if (isDeleted) 1 else 0,
      hidden = if (isHidden) 1 else 0,
      dueDate = dateTimeManager.fromRfc3339Format(task.due),
      notes = task.notes ?: "",
      parent = task.parent ?: "",
      position = task.position ?: "",
      updateDate = dateTimeManager.fromRfc3339Format(task.updated),
      status = task.status ?: "",
      uploaded = true
    )
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

    return googleTask.copy(
      selfLink = task.selfLink ?: "",
      kind = task.kind ?: "",
      eTag = task.etag ?: "",
      title = task.title ?: "",
      taskId = task.id ?: "",
      completeDate = dateTimeManager.fromRfc3339Format(task.completed),
      del = if (isDeleted) 1 else 0,
      hidden = if (isHidden) 1 else 0,
      dueDate = dateTimeManager.fromRfc3339Format(task.due),
      notes = task.notes ?: "",
      parent = task.parent ?: "",
      position = task.position ?: "",
      updateDate = dateTimeManager.fromRfc3339Format(task.updated),
      status = task.status ?: "",
      uploaded = true
    )
  }
}

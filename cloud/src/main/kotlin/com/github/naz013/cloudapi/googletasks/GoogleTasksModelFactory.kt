package com.github.naz013.cloudapi.googletasks

import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.TaskList
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

internal class GoogleTasksModelFactory(
  private val getRandomGoogleTaskListColorUseCase: GetRandomGoogleTaskListColorUseCase
) {

  fun toModel(googleTask: GoogleTask): Task {
    val task = Task()
    task.title = googleTask.title
    if (googleTask.notes.isNotEmpty()) {
      task.notes = googleTask.notes
    }
    if (googleTask.dueDate != 0L) {
      task.due = toRfc3339Format(googleTask.dueDate)
    }
    return task
  }

  fun toDomain(task: Task, listId: String): GoogleTask {
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
      completeDate = fromRfc3339Format(task.completed),
      del = if (isDeleted) 1 else 0,
      hidden = if (isHidden) 1 else 0,
      dueDate = fromRfc3339Format(task.due),
      notes = task.notes ?: "",
      parent = task.parent ?: "",
      position = task.position ?: "",
      updateDate = fromRfc3339Format(task.updated),
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
      completeDate = fromRfc3339Format(task.completed),
      del = if (isDeleted) 1 else 0,
      hidden = if (isHidden) 1 else 0,
      dueDate = fromRfc3339Format(task.due),
      notes = task.notes ?: "",
      parent = task.parent ?: "",
      position = task.position ?: "",
      updateDate = fromRfc3339Format(task.updated),
      status = task.status ?: "",
      uploaded = true
    )
  }

  fun toDomain(taskList: TaskList): GoogleTaskList {
    return toDomain(taskList, getRandomGoogleTaskListColorUseCase())
  }

  fun toDomain(taskList: TaskList, color: Int): GoogleTaskList {
    return GoogleTaskList(
      color = color,
      title = taskList.title ?: "",
      listId = taskList.id ?: "",
      eTag = taskList.etag ?: "",
      kind = taskList.kind ?: "",
      selfLink = taskList.selfLink ?: "",
      updated = fromRfc3339Format(taskList.updated),
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
      updated = fromRfc3339Format(taskList.updated),
      uploaded = true
    )
  }

  fun toRfc3339Format(millis: Long): String {
    return ZonedDateTime.of(
      LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()),
      ZoneId.systemDefault()
    ).format(RFC3339_DATE_FORMATTER)
  }

  private fun fromRfc3339Format(date: String?): Long {
    if (date == null) return 0L
    val dateTime = try {
      ZonedDateTime.parse(date, RFC3339_DATE_FORMATTER)
    } catch (e: Exception) {
      ZonedDateTime.parse(date)
    }
    return dateTime.toInstant().toEpochMilli()
  }

  companion object {
    private val RFC3339_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  }
}

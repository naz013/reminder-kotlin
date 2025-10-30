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

  /**
   * Converts a domain GoogleTask to Google Tasks API Task.
   *
   * @param googleTask The domain GoogleTask to convert
   * @return Google Tasks API Task object
   */
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

  /**
   * Converts a Google Tasks API Task to domain GoogleTask.
   *
   * @param task The Task from Google Tasks API
   * @param listId The ID of the task list containing this task
   * @return Domain GoogleTask object
   */
  fun toDomain(task: Task, listId: String): GoogleTask {
    val isDeleted = task.deleted ?: false
    val isHidden = task.hidden ?: false

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

  /**
   * Updates a domain GoogleTask with data from Google Tasks API Task.
   *
   * @param googleTask The existing domain GoogleTask to update
   * @param task The Task from Google Tasks API with new data
   * @return Updated domain GoogleTask
   */
  fun update(googleTask: GoogleTask, task: Task): GoogleTask {
    val isDeleted = task.deleted ?: false
    val isHidden = task.hidden ?: false

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

  /**
   * Converts a Google Tasks API TaskList to domain GoogleTaskList with random color.
   *
   * @param taskList The TaskList from Google Tasks API
   * @return Domain GoogleTaskList with random color
   */
  fun toDomain(taskList: TaskList): GoogleTaskList {
    return toDomain(taskList, getRandomGoogleTaskListColorUseCase())
  }

  /**
   * Converts a Google Tasks API TaskList to domain GoogleTaskList with specified color.
   *
   * @param taskList The TaskList from Google Tasks API
   * @param color The color code for the task list
   * @return Domain GoogleTaskList
   */
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

  /**
   * Updates a domain GoogleTaskList with data from Google Tasks API TaskList.
   *
   * @param googleTaskList The existing domain GoogleTaskList to update
   * @param taskList The TaskList from Google Tasks API with new data
   * @return Updated domain GoogleTaskList
   */
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

  /**
   * Converts milliseconds timestamp to RFC3339 formatted date string.
   *
   * @param millis The timestamp in milliseconds
   * @return RFC3339 formatted date string
   */
  fun toRfc3339Format(millis: Long): String {
    return ZonedDateTime.of(
      LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()),
      ZoneId.systemDefault()
    ).format(RFC3339_DATE_FORMATTER)
  }

  /**
   * Converts RFC3339 formatted date string to milliseconds timestamp.
   *
   * @param date The RFC3339 formatted date string, or null
   * @return Milliseconds timestamp, or 0 if date is null
   */
  private fun fromRfc3339Format(date: String?): Long {
    if (date == null) return 0L
    val dateTime = try {
      ZonedDateTime.parse(date, RFC3339_DATE_FORMATTER)
    } catch (_: Exception) {
      ZonedDateTime.parse(date)
    }
    return dateTime.toInstant().toEpochMilli()
  }

  companion object {
    private val RFC3339_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  }
}

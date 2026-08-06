package com.github.naz013.appfunctions.googletask

import com.github.naz013.appfunctions.toThreeTen
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository
import java.time.LocalDateTime as JavaLocalDateTime

/** Creates a task directly through [GoogleTasksApi] (the default list is used when none of the
 * caller's own task lists is passed) so the result carries the server-assigned id immediately -
 * unlike the reminder/note/birthday use cases, a locally-saved-only Google Task would never
 * appear in the user's real Google Tasks list. */
class CreateGoogleTaskUseCase(
  private val googleTasksApi: GoogleTasksApi,
  private val googleTaskRepository: GoogleTaskRepository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(
    title: String,
    notes: String?,
    dueDateTime: JavaLocalDateTime?,
  ): GoogleTask? {
    val task =
      GoogleTask(
        title = title,
        notes = notes ?: "",
        dueDate = dueDateTime?.let { dateTimeManager.toMillis(it.toThreeTen()) } ?: 0L,
        status = GoogleTask.TASKS_NEED_ACTION,
      )
    val saved = googleTasksApi.saveTask(task) ?: return null
    googleTaskRepository.save(saved)
    return saved
  }
}

package com.github.naz013.appfunctions.googletask

import com.github.naz013.appfunctions.toThreeTen
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository
import java.time.LocalDateTime as JavaLocalDateTime

/** Deliberately leaves [GoogleTask.status] untouched, unlike the UI's edit flow (which resets it to
 * [GoogleTask.TASKS_NEED_ACTION] on every save) - an AI-driven "change the due date" shouldn't
 * silently un-complete an already-finished task. */
class UpdateGoogleTaskUseCase(
  private val googleTasksApi: GoogleTasksApi,
  private val googleTaskRepository: GoogleTaskRepository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(
    id: String,
    title: String,
    notes: String?,
    dueDateTime: JavaLocalDateTime?,
  ): GoogleTask? {
    val existing = googleTaskRepository.getById(id) ?: return null
    val updated =
      existing.copy(
        title = title,
        notes = notes ?: "",
        dueDate = dueDateTime?.let { dateTimeManager.toMillis(it.toThreeTen()) } ?: 0L,
      )
    return googleTasksApi.updateTask(updated)?.also { googleTaskRepository.save(it) }
  }
}

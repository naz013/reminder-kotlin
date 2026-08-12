package com.github.naz013.appfunctions.googletask

import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository

/** Reads from the local, already-synced task list rather than hitting [com.github.naz013.cloudapi.googletasks.GoogleTasksApi]
 * directly - consistent with how the reminder/note/birthday list functions read local Room state. */
internal class ListGoogleTasksUseCase(
  private val googleTaskRepository: GoogleTaskRepository,
) {
  suspend operator fun invoke(includeCompleted: Boolean): List<GoogleTask> {
    val all = googleTaskRepository.getAll()
    return if (includeCompleted) all else all.filter { it.isNeedAction() }
  }
}

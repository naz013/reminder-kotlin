package com.github.naz013.appfunctions.googletask

import com.github.naz013.domain.GoogleTask
import com.github.naz013.usecase.googletasks.GetAllGoogleTasksUseCase

/** Reads from the local, already-synced task list rather than hitting [com.github.naz013.cloudapi.googletasks.GoogleTasksApi]
 * directly - consistent with how the reminder/note/birthday list functions read local Room state. */
class ListGoogleTasksUseCase(
  private val getAllGoogleTasksUseCase: GetAllGoogleTasksUseCase,
) {
  suspend operator fun invoke(includeCompleted: Boolean): List<GoogleTask> {
    val all = getAllGoogleTasksUseCase()
    return if (includeCompleted) all else all.filter { it.isNeedAction() }
  }
}

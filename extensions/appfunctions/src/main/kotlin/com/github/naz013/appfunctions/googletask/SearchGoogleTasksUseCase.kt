package com.github.naz013.appfunctions.googletask

import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository

/** Local-cache only, same characteristic as [ListGoogleTasksUseCase] - no live call to
 * [com.github.naz013.cloudapi.googletasks.GoogleTasksApi], so results can be stale relative to the
 * real Google Tasks account if the app hasn't synced recently. */
class SearchGoogleTasksUseCase(
  private val googleTaskRepository: GoogleTaskRepository,
) {
  suspend operator fun invoke(query: String): List<GoogleTask> = googleTaskRepository.search(query)
}

package com.github.naz013.appfunctions.googletask

import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository

class CompleteGoogleTaskUseCase(
  private val googleTasksApi: GoogleTasksApi,
  private val googleTaskRepository: GoogleTaskRepository,
) {
  suspend operator fun invoke(id: String): GoogleTask? {
    val existing = googleTaskRepository.getById(id) ?: return null
    return googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, existing)?.also {
      googleTaskRepository.save(it)
    }
  }
}

package com.github.naz013.appfunctions.googletask

import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository

class DeleteGoogleTaskUseCase(
  private val googleTasksApi: GoogleTasksApi,
  private val googleTaskRepository: GoogleTaskRepository,
) {
  suspend operator fun invoke(id: String): GoogleTask? {
    val existing = googleTaskRepository.getById(id) ?: return null
    return if (googleTasksApi.deleteTask(existing)) {
      googleTaskRepository.delete(id)
      existing
    } else {
      null
    }
  }
}

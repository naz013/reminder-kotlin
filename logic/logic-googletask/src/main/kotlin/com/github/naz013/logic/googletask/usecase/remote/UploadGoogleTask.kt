package com.github.naz013.logic.googletask.usecase.remote

import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository

internal class UploadGoogleTask(
  private val googleTasksApi: GoogleTasksApi,
  private val googleTaskRepository: GoogleTaskRepository,
) {
  suspend operator fun invoke(googleTask: GoogleTask) {
    googleTasksApi.updateTask(googleTask)?.let {
      googleTaskRepository.save(it)
    }
  }
}

package com.github.naz013.usecase.googletasks

import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository

class GetGoogleTaskByIdUseCase(
  private val googleTaskRepository: GoogleTaskRepository
) {

  suspend operator fun invoke(taskId: String): GoogleTask? {
    return googleTaskRepository.getById(taskId)
  }
}

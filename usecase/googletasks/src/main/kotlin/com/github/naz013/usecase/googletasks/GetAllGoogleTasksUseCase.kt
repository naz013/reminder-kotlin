package com.github.naz013.usecase.googletasks

import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository

class GetAllGoogleTasksUseCase(
  private val googleTaskRepository: GoogleTaskRepository
) {

  suspend operator fun invoke(): List<GoogleTask> {
    return googleTaskRepository.getAll()
  }
}

package com.github.naz013.usecase.googletasks

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskListRepository

class GetAllGoogleTaskListsUseCase(
  private val googleTaskListRepository: GoogleTaskListRepository
) {

  suspend operator fun invoke(): List<GoogleTaskList> {
    return googleTaskListRepository.getAll()
  }
}

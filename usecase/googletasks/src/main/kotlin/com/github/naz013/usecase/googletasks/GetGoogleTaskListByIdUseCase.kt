package com.github.naz013.usecase.googletasks

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskListRepository

class GetGoogleTaskListByIdUseCase(
  private val googleTaskListRepository: GoogleTaskListRepository
) {

  suspend operator fun invoke(listId: String): GoogleTaskList? {
    return googleTaskListRepository.getById(listId)
  }
}

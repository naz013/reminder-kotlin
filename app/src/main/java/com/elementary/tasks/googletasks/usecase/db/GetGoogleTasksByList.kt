package com.elementary.tasks.googletasks.usecase.db

import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskRepository

class GetGoogleTasksByList(
  private val googleTaskRepository: GoogleTaskRepository
) {

  suspend operator fun invoke(list: GoogleTaskList): List<GoogleTask> {
    return googleTaskRepository.getAllByList(list.listId)
  }
}

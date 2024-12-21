package com.elementary.tasks.googletasks.usecase.db

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskListRepository

class SaveGoogleTaskList(
  private val googleTaskListRepository: GoogleTaskListRepository
) {

  suspend operator fun invoke(googleTaskList: GoogleTaskList) {
    googleTaskListRepository.save(googleTaskList)
  }
}

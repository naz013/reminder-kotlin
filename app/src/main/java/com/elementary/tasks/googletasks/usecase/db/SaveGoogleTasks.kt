package com.elementary.tasks.googletasks.usecase.db

import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository

class SaveGoogleTasks(
  private val googleTaskRepository: GoogleTaskRepository
) {

  suspend operator fun invoke(tasks: List<GoogleTask>) {
    googleTaskRepository.saveAll(tasks)
  }
}

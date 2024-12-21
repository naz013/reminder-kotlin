package com.elementary.tasks.googletasks.usecase.remote

import com.elementary.tasks.core.cloud.GTasks
import com.github.naz013.domain.GoogleTask

class UploadGoogleTask(
  private val gTasks: GTasks
) {

  operator fun invoke(googleTask: GoogleTask) {
    gTasks.updateTask(googleTask)
  }
}

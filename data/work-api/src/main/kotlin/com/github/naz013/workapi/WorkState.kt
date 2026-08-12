package com.github.naz013.workapi

sealed class WorkState {
  data object Enqueued : WorkState()

  data class Running(
    val progress: TaskData,
  ) : WorkState()

  data object Succeeded : WorkState()

  data object Failed : WorkState()

  data object Cancelled : WorkState()

  data object Blocked : WorkState()
}

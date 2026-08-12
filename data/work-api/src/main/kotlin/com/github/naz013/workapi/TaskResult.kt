package com.github.naz013.workapi

sealed class TaskResult {
  data object Success : TaskResult()

  data object Retry : TaskResult()

  data object Failure : TaskResult()
}

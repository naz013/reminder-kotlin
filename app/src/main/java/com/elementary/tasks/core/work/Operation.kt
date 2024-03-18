package com.elementary.tasks.core.work

interface Operation {
  suspend fun process(): OperationResult
}

sealed class OperationResult {
  data object Success : OperationResult()
  data object Failed : OperationResult()
}

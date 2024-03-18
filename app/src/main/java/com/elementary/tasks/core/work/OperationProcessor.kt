package com.elementary.tasks.core.work

class OperationProcessor(
  private val operations: List<Operation>
) : Operation {
  override suspend fun process(): OperationResult {
    return if (operations.map { it.process() }.all { it is OperationResult.Success }) {
      OperationResult.Success
    } else {
      OperationResult.Failed
    }
  }
}

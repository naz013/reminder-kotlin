package com.github.naz013.workapi

fun interface TaskProgressReporter {
  suspend fun report(data: TaskData)

  companion object {
    val NONE =
      TaskProgressReporter { }
  }
}

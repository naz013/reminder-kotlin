package com.elementary.tasks.core.protocol

object WeekDaysProtocol {

  fun getWorkDays(): List<Int> {
    return listOf(0, 1, 1, 1, 1, 1, 0)
  }

  fun getWeekend(): List<Int> {
    return listOf(1, 0, 0, 0, 0, 0, 1)
  }

  fun getAllDays(): List<Int> {
    return listOf(1, 1, 1, 1, 1, 1, 1)
  }
}

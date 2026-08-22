package com.github.naz013.domain.routine

object RoutineStepComparator : Comparator<RoutineStep> {
  override fun compare(a: RoutineStep, b: RoutineStep): Int {
    val timeA = a.scheduledTime
    val timeB = b.scheduledTime
    return when {
      timeA != null && timeB != null -> {
        val timeComparison = timeA.compareTo(timeB)
        if (timeComparison != 0) timeComparison else a.order.compareTo(b.order)
      }
      timeA != null && timeB == null -> -1
      timeA == null && timeB != null -> 1
      else -> a.order.compareTo(b.order)
    }
  }
}

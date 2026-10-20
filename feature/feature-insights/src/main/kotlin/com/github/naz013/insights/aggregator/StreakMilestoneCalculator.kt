package com.github.naz013.insights.aggregator

internal data class StreakMilestone(
  val reachedDays: Int?,
  val nextDays: Int?,
  val daysToNext: Int,
  val progress: Float
)

internal object StreakMilestoneCalculator {

  val MILESTONES = listOf(7, 30, 100)

  fun calculate(currentStreakDays: Int): StreakMilestone {
    val streak = currentStreakDays.coerceAtLeast(0)
    val reached = MILESTONES.lastOrNull { streak >= it }
    val next = MILESTONES.firstOrNull { streak < it }
    if (next == null) {
      return StreakMilestone(reachedDays = reached, nextDays = null, daysToNext = 0, progress = 1f)
    }
    val previous = reached ?: 0
    val progress = (streak - previous).toFloat() / (next - previous).toFloat()
    return StreakMilestone(
      reachedDays = reached,
      nextDays = next,
      daysToNext = next - streak,
      progress = progress.coerceIn(0f, 1f)
    )
  }
}

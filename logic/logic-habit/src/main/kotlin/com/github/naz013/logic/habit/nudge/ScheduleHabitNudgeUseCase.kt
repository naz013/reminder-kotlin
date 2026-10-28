package com.github.naz013.logic.habit.nudge

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.logic.routine.HabitConfig
import com.github.naz013.scheduler.JobSchedulerApi
import org.threeten.bp.ZoneId

class ScheduleHabitNudgeUseCase(
  private val jobScheduler: JobSchedulerApi,
  private val calculator: HabitNudgeCalculator,
  private val nowDateTimeProvider: NowDateTimeProvider,
  private val habitConfig: HabitConfig
) {
  operator fun invoke(habit: Habit) {
    val next = if (habitConfig.isEnabled) calculator.nextTrigger(habit, nowDateTimeProvider.nowDateTime()) else null
    if (next == null) {
      jobScheduler.cancelHabitNudge(habit.id)
      return
    }
    val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    jobScheduler.scheduleHabitNudge(habit.id, millis)
  }
}

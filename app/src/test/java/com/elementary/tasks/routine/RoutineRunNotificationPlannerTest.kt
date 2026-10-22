package com.elementary.tasks.routine

import com.github.naz013.feature.routine.run.RoutineRunState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineRunNotificationPlannerTest {

  private fun state(
    stepIndex: Int = 1,
    stepTitle: String = "Stretch",
    isTimed: Boolean = true,
    remaining: Int = 90,
    isPaused: Boolean = false,
    completedCount: Int = 1,
    stepCount: Int = 4,
  ) = RoutineRunState.Running(
    routineId = "r1",
    routineTitle = "Morning",
    stepIndex = stepIndex,
    stepCount = stepCount,
    stepTitle = stepTitle,
    scheduledTimeLabel = null,
    isTimed = isTimed,
    remainingSeconds = remaining,
    stepTotalSeconds = if (isTimed) 120 else 0,
    isPaused = isPaused,
    completedCount = completedCount,
  )

  @Test
  fun `running timed step uses chronometer ending at now plus remaining`() {
    val plan = RoutineRunNotificationPlanner.plan(state(remaining = 90), nowMillis = 10_000L, sdkInt = 35)

    assertTrue(plan.useChronometer)
    assertEquals(100_000L, plan.endTimeMillis)
    assertNull(plan.pausedTimeLabel)
    assertTrue(plan.showToggleAction)
    assertEquals(2, plan.stepNumber)
  }

  @Test
  fun `paused timed step shows static time and no chronometer`() {
    val plan = RoutineRunNotificationPlanner.plan(state(remaining = 75, isPaused = true), 0L, 35)

    assertFalse(plan.useChronometer)
    assertEquals("01:15", plan.pausedTimeLabel)
    assertTrue(plan.showToggleAction)
  }

  @Test
  fun `untimed step shows static text without chronometer or toggle action`() {
    val plan = RoutineRunNotificationPlanner.plan(state(isTimed = false, remaining = 0), 0L, 35)

    assertFalse(plan.useChronometer)
    assertNull(plan.pausedTimeLabel)
    assertFalse(plan.showToggleAction)
  }

  @Test
  fun `api 35 is not promoted and has no progress`() {
    val plan = RoutineRunNotificationPlanner.plan(state(), 0L, 35)

    assertFalse(plan.promoted)
    assertFalse(plan.showProgress)
  }

  @Test
  fun `api 36 is promoted with progress by completed steps`() {
    val plan = RoutineRunNotificationPlanner.plan(state(completedCount = 2, stepCount = 4), 0L, 36)

    assertTrue(plan.promoted)
    assertTrue(plan.showProgress)
    assertEquals(2, plan.completedCount)
    assertEquals(4, plan.stepCount)
  }

  @Test
  fun `reposts when nothing was posted yet`() {
    val current = RoutineRunNotificationPlanner.snapshot(state(), 0L)

    assertTrue(RoutineRunNotificationPlanner.shouldRepost(null, current))
  }

  @Test
  fun `does not repost on a per second tick of a running step`() {
    val last = RoutineRunNotificationPlanner.snapshot(state(remaining = 90), 0L)
    val current = RoutineRunNotificationPlanner.snapshot(state(remaining = 89), 1_000L)

    assertFalse(RoutineRunNotificationPlanner.shouldRepost(last, current))
  }

  @Test
  fun `reposts when the step changes`() {
    val last = RoutineRunNotificationPlanner.snapshot(state(stepIndex = 1, stepTitle = "Stretch"), 0L)
    val current = RoutineRunNotificationPlanner.snapshot(state(stepIndex = 2, stepTitle = "Run", completedCount = 2), 0L)

    assertTrue(RoutineRunNotificationPlanner.shouldRepost(last, current))
  }

  @Test
  fun `reposts on pause and on resume`() {
    val running = RoutineRunNotificationPlanner.snapshot(state(remaining = 80), 0L)
    val paused = RoutineRunNotificationPlanner.snapshot(state(remaining = 80, isPaused = true), 0L)
    val resumed = RoutineRunNotificationPlanner.snapshot(state(remaining = 80), 5_000L)

    assertTrue(RoutineRunNotificationPlanner.shouldRepost(running, paused))
    assertTrue(RoutineRunNotificationPlanner.shouldRepost(paused, resumed))
  }

  @Test
  fun `does not repost while paused with unchanged remaining time`() {
    val last = RoutineRunNotificationPlanner.snapshot(state(remaining = 80, isPaused = true), 0L)
    val current = RoutineRunNotificationPlanner.snapshot(state(remaining = 80, isPaused = true), 9_000L)

    assertFalse(RoutineRunNotificationPlanner.shouldRepost(last, current))
  }

  @Test
  fun `reposts when a minute is added to a running step`() {
    val last = RoutineRunNotificationPlanner.snapshot(state(remaining = 80), 0L)
    val current = RoutineRunNotificationPlanner.snapshot(state(remaining = 139), 1_000L)

    assertTrue(RoutineRunNotificationPlanner.shouldRepost(last, current))
  }

  @Test
  fun `reposts when a step is skipped to the next one with the same title`() {
    val last = RoutineRunNotificationPlanner.snapshot(state(stepIndex = 0, stepTitle = "Same", completedCount = 0), 0L)
    val current = RoutineRunNotificationPlanner.snapshot(state(stepIndex = 1, stepTitle = "Same", completedCount = 0), 0L)

    assertTrue(RoutineRunNotificationPlanner.shouldRepost(last, current))
  }

  @Test
  fun `does not repost on an untimed step tick`() {
    val last = RoutineRunNotificationPlanner.snapshot(state(isTimed = false, remaining = 0), 0L)
    val current = RoutineRunNotificationPlanner.snapshot(state(isTimed = false, remaining = 0), 30_000L)

    assertFalse(RoutineRunNotificationPlanner.shouldRepost(last, current))
  }
}

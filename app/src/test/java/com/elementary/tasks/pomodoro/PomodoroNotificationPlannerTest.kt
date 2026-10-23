package com.elementary.tasks.pomodoro

import com.github.naz013.feature.pomodoro.engine.PomodoroEngineState
import com.github.naz013.feature.pomodoro.engine.PomodoroMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PomodoroNotificationPlannerTest {

  private fun state(
    remaining: Int = 600,
    total: Int = 1500,
    paused: Boolean = false,
    mode: PomodoroMode = PomodoroMode.Work,
  ) = PomodoroEngineState(
    mode = mode,
    isRunning = true,
    isPaused = paused,
    remainingSeconds = remaining,
    totalSeconds = total,
  )

  @Test
  fun `computes end time and elapsed progress while running`() {
    val plan = PomodoroNotificationPlanner.plan(state(), nowMillis = 10_000L, sdkInt = 36)

    assertTrue(plan.useChronometer)
    assertEquals(10_000L + 600_000L, plan.endTimeMillis)
    assertEquals(900, plan.progressElapsed)
    assertEquals(1500, plan.progressMax)
    assertEquals("10:00", plan.timeLabel)
  }

  @Test
  fun `disables chronometer when paused`() {
    val plan = PomodoroNotificationPlanner.plan(state(paused = true), 0L, 36)

    assertFalse(plan.useChronometer)
  }

  @Test
  fun `skips progress and promotion below API 36`() {
    val plan = PomodoroNotificationPlanner.plan(state(), 0L, 35)

    assertFalse(plan.promoted)
    assertFalse(plan.showProgress)
  }

  @Test
  fun `falls back to plain chronometer when promoted notifications are not allowed`() {
    val plan = PomodoroNotificationPlanner.plan(state(), 0L, 36, canPostPromoted = false)

    assertFalse(plan.promoted)
    assertFalse(plan.showProgress)
    assertTrue(plan.useChronometer)
  }

  @Test
  fun `formats time independent of default locale`() {
    val previous = java.util.Locale.getDefault()
    java.util.Locale.setDefault(java.util.Locale.forLanguageTag("ar-EG"))
    try {
      assertEquals("01:05", PomodoroNotificationPlanner.formatTime(65))
    } finally {
      java.util.Locale.setDefault(previous)
    }
  }

  @Test
  fun `skips progress when total is unknown`() {
    val plan = PomodoroNotificationPlanner.plan(state(remaining = 0, total = 0), 0L, 36)

    assertFalse(plan.showProgress)
    assertTrue(plan.promoted)
  }

  @Test
  fun `reposts when nothing was posted yet`() {
    val current = PomodoroNotificationPlanner.snapshot(state(), 0L)

    assertTrue(PomodoroNotificationPlanner.shouldRepost(null, current))
  }

  @Test
  fun `does not repost on a normal one second tick`() {
    val last = PomodoroNotificationPlanner.snapshot(state(remaining = 600), 0L)
    val current = PomodoroNotificationPlanner.snapshot(state(remaining = 599), 1_000L)

    assertFalse(PomodoroNotificationPlanner.shouldRepost(last, current))
  }

  @Test
  fun `reposts when end time drifts beyond tolerance`() {
    val last = PomodoroNotificationPlanner.snapshot(state(remaining = 600), 0L)
    val current = PomodoroNotificationPlanner.snapshot(state(remaining = 600), 10_000L)

    assertTrue(PomodoroNotificationPlanner.shouldRepost(last, current))
  }

  @Test
  fun `reposts on pause and on mode change`() {
    val last = PomodoroNotificationPlanner.snapshot(state(), 0L)

    assertTrue(PomodoroNotificationPlanner.shouldRepost(last, PomodoroNotificationPlanner.snapshot(state(paused = true), 0L)))
    assertTrue(
      PomodoroNotificationPlanner.shouldRepost(
        last,
        PomodoroNotificationPlanner.snapshot(state(mode = PomodoroMode.Break), 0L),
      ),
    )
  }

  @Test
  fun `does not repost while paused with unchanged remaining time`() {
    val last = PomodoroNotificationPlanner.snapshot(state(paused = true), 0L)
    val current = PomodoroNotificationPlanner.snapshot(state(paused = true), 5_000L)

    assertFalse(PomodoroNotificationPlanner.shouldRepost(last, current))
  }
}

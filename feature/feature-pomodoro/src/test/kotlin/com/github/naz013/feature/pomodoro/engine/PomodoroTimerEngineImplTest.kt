package com.github.naz013.feature.pomodoro.engine

import com.github.naz013.feature.pomodoro.PomodoroForegroundNotifier
import com.github.naz013.feature.pomodoro.PomodoroPreferences
import com.github.naz013.feature.pomodoro.usecase.RecordPomodoroSessionUseCase
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PomodoroTimerEngineImplTest {

  private val pomodoroPreferences = mockk<PomodoroPreferences>()
  private val pomodoroForegroundNotifier = mockk<PomodoroForegroundNotifier>(relaxed = true)
  private val recordPomodoroSessionUseCase = mockk<RecordPomodoroSessionUseCase>(relaxed = true)

  private lateinit var engine: PomodoroTimerEngineImpl

  @Before
  fun setUp() {
    every { pomodoroPreferences.workDurationMinutes } returns 25
    every { pomodoroPreferences.breakDurationMinutes } returns 5
    every { pomodoroPreferences.longBreakDurationMinutes } returns 15
    every { pomodoroPreferences.sessionsUntilLongBreak } returns 4
    every { pomodoroPreferences.autoStartNext } returns false
    coEvery { recordPomodoroSessionUseCase(any(), any(), any(), any(), any()) } returns Unit

    engine = PomodoroTimerEngineImpl(
      dispatcherProvider = mockDispatcherProvider(),
      pomodoroPreferences = pomodoroPreferences,
      pomodoroForegroundNotifier = pomodoroForegroundNotifier,
      recordPomodoroSessionUseCase = recordPomodoroSessionUseCase,
    )
  }

  @Test
  fun `start begins a work session for the configured duration`() {
    engine.start(linkedReminderId = "r1")

    val state = engine.state.value
    assertEquals(PomodoroMode.Work, state.mode)
    assertTrue(state.isRunning)
    assertFalse(state.isPaused)
    assertEquals(25 * 60, state.remainingSeconds)
    assertEquals("r1", state.linkedReminderId)
  }

  @Test
  fun `starting a session shows the foreground notification`() {
    engine.start(linkedReminderId = null)

    coVerify { pomodoroForegroundNotifier.start() }
  }

  @Test
  fun `pause freezes the countdown and marks the state paused`() {
    engine.start(linkedReminderId = null)

    engine.pause()

    assertTrue(engine.state.value.isPaused)
  }

  @Test
  fun `resume clears the paused flag`() {
    engine.start(linkedReminderId = null)
    engine.pause()

    engine.resume()

    assertFalse(engine.state.value.isPaused)
  }

  @Test
  fun `stopping mid-work records an interrupted session`() {
    engine.start(linkedReminderId = "r1")

    engine.stop(discard = false)

    coVerify {
      recordPomodoroSessionUseCase(
        linkedReminderId = "r1",
        startedAt = any(),
        plannedFocusSeconds = 25 * 60,
        actualFocusSeconds = any(),
        wasCompleted = false,
      )
    }
    assertEquals(PomodoroMode.Idle, engine.state.value.mode)
    assertFalse(engine.state.value.isRunning)
  }

  @Test
  fun `discarding a stopped session does not record it`() {
    engine.start(linkedReminderId = null)

    engine.stop(discard = true)

    coVerify(exactly = 0) { recordPomodoroSessionUseCase(any(), any(), any(), any(), any()) }
  }

  @Test
  fun `stop always tears down the foreground notification`() {
    engine.start(linkedReminderId = null)

    engine.stop(discard = false)

    coVerify { pomodoroForegroundNotifier.stop() }
  }

  @Test
  fun `skipping to break records the work session and switches mode`() {
    engine.start(linkedReminderId = null)

    engine.skipToBreak()

    coVerify { recordPomodoroSessionUseCase(any(), any(), any(), any(), wasCompleted = false) }
    assertEquals(PomodoroMode.Break, engine.state.value.mode)
    assertEquals(5 * 60, engine.state.value.remainingSeconds)
  }

  @Test
  fun `skipping to break is a no-op outside a work session`() {
    engine.skipToBreak()

    assertEquals(PomodoroMode.Idle, engine.state.value.mode)
    coVerify(exactly = 0) { recordPomodoroSessionUseCase(any(), any(), any(), any(), any()) }
  }
}

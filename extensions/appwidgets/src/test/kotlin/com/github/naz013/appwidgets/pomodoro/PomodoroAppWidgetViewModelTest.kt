package com.github.naz013.appwidgets.pomodoro

import com.github.naz013.common.TextProvider
import com.github.naz013.domain.pomodoro.PomodoroSessionRecord
import com.github.naz013.feature.pomodoro.engine.PomodoroEngineState
import com.github.naz013.feature.pomodoro.engine.PomodoroMode
import com.github.naz013.feature.pomodoro.engine.PomodoroTimerEngine
import com.github.naz013.platform.BuildInfo
import com.github.naz013.repository.PomodoroSessionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class PomodoroAppWidgetViewModelTest {

  private val buildInfo = mockk<BuildInfo>()
  private val engine = mockk<PomodoroTimerEngine>()
  private val engineState = MutableStateFlow(PomodoroEngineState())
  private val repository = mockk<PomodoroSessionRepository>()
  private val textProvider = mockk<TextProvider>()
  private val timeFormatter = mockk<PomodoroWidgetTimeFormatter>()
  private val viewModel = PomodoroAppWidgetViewModel(
    buildInfo,
    engine,
    repository,
    textProvider,
    timeFormatter,
    nowMillis = { NOW }
  )

  @Before
  fun setUp() {
    every { buildInfo.isPro } returns true
    every { engine.state } returns engineState
    every { textProvider.getString(any()) } returns "label"
    every { textProvider.getString(any(), *anyVararg()) } answers {
      (args[1] as Array<*>).joinToString("/")
    }
    every { timeFormatter.formatClock(any()) } answers { "clock${firstArg<Long>()}" }
    coEvery { repository.getByDateRange(any(), any()) } returns emptyList()
  }

  private fun record(seconds: Int) = PomodoroSessionRecord(
    startedAt = LocalDateTime.of(2026, 10, 1, 8, 0),
    plannedFocusSeconds = seconds,
    actualFocusSeconds = seconds,
    wasCompleted = true
  )

  @Test
  fun `returns locked state when the build is not pro`() = runTest {
    every { buildInfo.isPro } returns false

    val state = viewModel.getState()

    assertFalse(state.isUnlocked)
  }

  @Test
  fun `returns idle state with today focus minutes when nothing is running`() = runTest {
    coEvery { repository.getByDateRange(any(), any()) } returns listOf(record(1500), record(600))

    val state = viewModel.getState()

    assertTrue(state.isIdle)
    assertEquals("35", state.focusTodayLabel)
  }

  @Test
  fun `returns end time for a running work session`() = runTest {
    engineState.value = PomodoroEngineState(
      mode = PomodoroMode.Work,
      isRunning = true,
      remainingSeconds = 600
    )

    val state = viewModel.getState()

    assertEquals(PomodoroMode.Work, state.mode)
    assertFalse(state.isPaused)
    assertEquals("clock${NOW + 600_000L}", state.timeLabel)
  }

  @Test
  fun `returns remaining time instead of end time when paused`() = runTest {
    engineState.value = PomodoroEngineState(
      mode = PomodoroMode.Break,
      isRunning = true,
      isPaused = true,
      remainingSeconds = 125
    )

    val state = viewModel.getState()

    assertTrue(state.isPaused)
    assertEquals("2:05", state.timeLabel)
  }

  private companion object {
    const val NOW = 1_000_000L
  }
}

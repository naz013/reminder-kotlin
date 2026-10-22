package com.github.naz013.feature.routine.execution

import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logic.routine.RoutineDurationCalculator
import com.github.naz013.logic.routine.usecase.RecordRoutineExecutionUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.preferences.AppPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineExecutionViewModelTest : BaseTest() {
  private val routineRepository = mockk<RoutineRepository>()
  private val recordUseCase = mockk<RecordRoutineExecutionUseCase>()
  private val durationCalculator = mockk<RoutineDurationCalculator>()
  private val appPreferences = mockk<AppPreferences>()

  private fun TestScope.createViewModel(
    steps: List<RoutineStep>,
    autoAdvance: Boolean = true,
  ): RoutineExecutionViewModel {
    val routine = Routine(
      id = ROUTINE_ID,
      title = "Morning",
      steps = steps,
      autoAdvance = autoAdvance,
      soundAlertsEnabled = false,
      createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
      updatedAt = LocalDateTime.of(2026, 1, 1, 0, 0),
    )
    coEvery { routineRepository.getById(ROUTINE_ID) } returns routine
    coEvery { recordUseCase(any(), any(), any(), any()) } returns mockk(relaxed = true)
    every { durationCalculator.formatDuration(any()) } answers { "${firstArg<Int>()}s" }
    every { appPreferences.hapticsEnabled } returns false

    val tickerDispatcher = StandardTestDispatcher(testScheduler)
    val dispatcherProvider = mockk<DispatcherProvider>()
    every { dispatcherProvider.default() } returns Dispatchers.Unconfined
    every { dispatcherProvider.io() } returns Dispatchers.Unconfined
    every { dispatcherProvider.main() } returns tickerDispatcher

    val viewModel = RoutineExecutionViewModel(
      id = ROUTINE_ID,
      dispatcherProvider = dispatcherProvider,
      routineRepository = routineRepository,
      recordRoutineExecutionUseCase = recordUseCase,
      routineDurationCalculator = durationCalculator,
      appPreferences = appPreferences,
      currentTimeMillis = { testScheduler.currentTime },
    )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      try {
        awaitCancellation()
      } finally {
        viewModel.viewModelScope.cancel()
      }
    }
    return viewModel
  }

  private fun RoutineExecutionViewModel.running() = state.value as RoutineExecutionState.Running

  private fun TestScope.advanceSeconds(seconds: Int) {
    advanceTimeBy(seconds * 1000L)
    runCurrent()
  }

  private fun step(id: String, seconds: Int, order: Int) =
    RoutineStep(id = id, title = id, durationSeconds = seconds, order = order)

  @Test
  fun `countdown ticks down from the absolute deadline`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 60, 0), step("b", 60, 1)))
    runCurrent()
    assertEquals(60, viewModel.running().remainingSeconds)

    advanceSeconds(10)

    assertEquals(50, viewModel.running().remainingSeconds)
    assertEquals("00:50", viewModel.running().timeLabel)
  }

  @Test
  fun `pause freezes remaining time and resume re-anchors the deadline`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 60, 0)))
    runCurrent()
    advanceSeconds(10)

    viewModel.onPlayPauseClick()
    advanceSeconds(30)
    assertTrue(viewModel.running().isPaused)
    assertEquals(50, viewModel.running().remainingSeconds)

    viewModel.onPlayPauseClick()
    advanceSeconds(5)

    assertFalse(viewModel.running().isPaused)
    assertEquals(45, viewModel.running().remainingSeconds)
  }

  @Test
  fun `add minute extends the remaining time`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 60, 0)))
    runCurrent()
    advanceSeconds(10)

    viewModel.onAddMinuteClick()

    assertEquals(110, viewModel.running().remainingSeconds)
  }

  @Test
  fun `skip does not mark the step completed`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 60, 0)))
    runCurrent()

    viewModel.onSkipClick()
    runCurrent()

    assertTrue(viewModel.state.value is RoutineExecutionState.Finished)
    coVerify { recordUseCase(ROUTINE_ID, emptyList(), any(), 1) }
  }

  @Test
  fun `complete marks the step completed`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 60, 0), step("b", 60, 1)))
    runCurrent()

    viewModel.onCompleteStepClick()
    assertEquals(1, viewModel.running().stepIndex)
    viewModel.onSkipClick()
    runCurrent()

    coVerify { recordUseCase(ROUTINE_ID, listOf("a"), any(), 2) }
  }

  @Test
  fun `expiry auto advances and marks the step completed`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 5, 0), step("b", 60, 1)))
    runCurrent()

    advanceSeconds(6)

    assertEquals(1, viewModel.running().stepIndex)
    viewModel.onCompleteStepClick()
    runCurrent()
    coVerify { recordUseCase(ROUTINE_ID, listOf("a", "b"), any(), 2) }
  }

  @Test
  fun `expiry does not advance when auto advance is off`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 5, 0), step("b", 60, 1)), autoAdvance = false)
    runCurrent()

    advanceSeconds(10)

    assertEquals(0, viewModel.running().stepIndex)
    assertEquals(0, viewModel.running().remainingSeconds)
  }

  @Test
  fun `finishing shows the summary and records once`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 60, 0)))
    runCurrent()
    advanceSeconds(20)

    viewModel.onCompleteStepClick()
    viewModel.onCompleteStepClick()
    runCurrent()

    val finished = viewModel.state.value as RoutineExecutionState.Finished
    assertEquals(1, finished.completedCount)
    assertEquals(1, finished.totalCount)
    assertEquals("20s", finished.totalTimeLabel)
    coVerify(exactly = 1) { recordUseCase(any(), any(), any(), any()) }
  }

  @Test
  fun `back press mid run records silently without a summary`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 60, 0), step("b", 60, 1)))
    runCurrent()
    val events = mutableListOf<RoutineExecutionViewModel.NavigationEvent>()
    viewModel.navigationEvent.observeForever { event -> event.getContentIfNotHandled()?.let(events::add) }

    viewModel.onCompleteStepClick()
    viewModel.onBackClick()
    runCurrent()

    assertTrue(viewModel.state.value is RoutineExecutionState.Running)
    assertEquals(listOf<RoutineExecutionViewModel.NavigationEvent>(RoutineExecutionViewModel.NavigationEvent.Back), events)
    coVerify(exactly = 1) { recordUseCase(ROUTINE_ID, listOf("a"), any(), 2) }
  }

  @Test
  fun `untimed step has no countdown and ignores timer controls`() = runTest {
    val viewModel = createViewModel(listOf(step("a", 0, 0)))
    runCurrent()

    viewModel.onPlayPauseClick()
    viewModel.onAddMinuteClick()
    advanceSeconds(30)

    val running = viewModel.running()
    assertFalse(running.isTimed)
    assertFalse(running.isPaused)
    assertEquals(0, running.remainingSeconds)
    viewModel.onCompleteStepClick()
    runCurrent()
    assertTrue(viewModel.state.value is RoutineExecutionState.Finished)
  }

  private companion object {
    const val ROUTINE_ID = "routine-1"
  }
}

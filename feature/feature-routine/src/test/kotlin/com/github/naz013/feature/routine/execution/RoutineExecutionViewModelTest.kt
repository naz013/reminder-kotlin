package com.github.naz013.feature.routine.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.github.naz013.feature.routine.run.RoutineRunEngine
import com.github.naz013.feature.routine.run.RoutineRunState
import com.github.naz013.logic.routine.RoutineDurationCalculator
import com.github.naz013.testing.BaseTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineExecutionViewModelTest : BaseTest() {
  private val engineState = MutableStateFlow<RoutineRunState>(RoutineRunState.Idle)
  private val transitions = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  private val engine = mockk<RoutineRunEngine>(relaxed = true) {
    every { start(any()) } returns ATTACH_TOKEN
    every { state } returns engineState
    every { stepTransitions } returns transitions
  }
  private val durationCalculator = mockk<RoutineDurationCalculator> {
    every { formatDuration(any()) } answers { "${firstArg<Int>()}s" }
  }

  private fun createViewModel() = RoutineExecutionViewModel(ROUTINE_ID, engine, durationCalculator)

  private fun running(
    stepIndex: Int = 0,
    isPaused: Boolean = false,
    remaining: Int = 30,
  ) = RoutineRunState.Running(
    routineId = ROUTINE_ID,
    routineTitle = "Morning",
    stepIndex = stepIndex,
    stepCount = 2,
    stepTitle = "Stretch",
    scheduledTimeLabel = null,
    isTimed = true,
    remainingSeconds = remaining,
    stepTotalSeconds = 60,
    isPaused = isPaused,
    stepEndsAtMillis = null,
    completedCount = 0,
  )

  private fun backNavigation() = listOf<RoutineExecutionViewModel.NavigationEvent>(
    RoutineExecutionViewModel.NavigationEvent.Back,
  )

  @Test
  fun `starts the run for its routine id on creation`() {
    createViewModel()

    verify { engine.start(ROUTINE_ID) }
  }

  @Test
  fun `maps running engine state to the screen state`() = runTest {
    val viewModel = createViewModel()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

    engineState.value = running(remaining = 30)

    val ui = viewModel.state.value as RoutineExecutionState.Running
    assertEquals("00:30", ui.timeLabel)
    assertEquals(0.5f, ui.progress, 0.001f)
    assertTrue(ui.isFirstStep)
  }

  @Test
  fun `maps finished engine state to the summary`() = runTest {
    val viewModel = createViewModel()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

    engineState.value = RoutineRunState.Finished(ROUTINE_ID, totalTimeSeconds = 20, completedCount = 1, totalCount = 2)

    val ui = viewModel.state.value as RoutineExecutionState.Finished
    assertEquals("20s", ui.totalTimeLabel)
    assertEquals(1, ui.completedCount)
  }

  @Test
  fun `keeps the last screen state after the engine goes idle`() = runTest {
    val viewModel = createViewModel()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
    engineState.value = running()

    engineState.value = RoutineRunState.Idle

    assertTrue(viewModel.state.value is RoutineExecutionState.Running)
  }

  @Test
  fun `play pause pauses a running step and resumes a paused one`() {
    val viewModel = createViewModel()
    engineState.value = running(isPaused = false)
    viewModel.onPlayPauseClick()
    verify(exactly = 1) { engine.pause() }

    engineState.value = running(isPaused = true)
    viewModel.onPlayPauseClick()
    verify(exactly = 1) { engine.resume() }
  }

  @Test
  fun `forwards add minute skip complete and previous to the engine`() {
    val viewModel = createViewModel()

    viewModel.onAddMinuteClick()
    viewModel.onSkipClick()
    viewModel.onCompleteStepClick()
    viewModel.onPreviousStepClick()

    verify { engine.addMinute() }
    verify { engine.skip() }
    verify { engine.completeStep() }
    verify { engine.previous() }
  }

  @Test
  fun `back press stops the run and navigates back`() {
    val viewModel = createViewModel()
    val events = mutableListOf<RoutineExecutionViewModel.NavigationEvent>()
    viewModel.navigationEvent.observeForever { event -> event.getContentIfNotHandled()?.let(events::add) }

    viewModel.onBackClick()

    verify(exactly = 1) { engine.stop() }
    assertEquals(backNavigation(), events)
  }

  @Test
  fun `navigates back and clears the engine when the routine is not found`() {
    val viewModel = createViewModel()
    val events = mutableListOf<RoutineExecutionViewModel.NavigationEvent>()
    viewModel.navigationEvent.observeForever { event -> event.getContentIfNotHandled()?.let(events::add) }

    engineState.value = RoutineRunState.NotFound(ROUTINE_ID)

    verify { engine.stop() }
    assertEquals(backNavigation(), events)
  }

  @Test
  fun `forwards step transitions as haptic events`() {
    val viewModel = createViewModel()
    var handled = 0
    viewModel.stepTransitionEvent.observeForever { event -> event.getContentIfNotHandled()?.let { handled++ } }

    transitions.tryEmit(Unit)

    assertEquals(1, handled)
  }

  @Test
  fun `releases the run with its own attach token when cleared`() {
    val store = ViewModelStore()
    val factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T = createViewModel() as T
    }
    ViewModelProvider(store, factory)[RoutineExecutionViewModel::class.java]

    store.clear()

    verify(exactly = 1) { engine.release(ATTACH_TOKEN) }
  }

  private companion object {
    const val ROUTINE_ID = "routine-1"
    const val ATTACH_TOKEN = 7L
  }
}

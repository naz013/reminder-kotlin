package com.github.naz013.feature.routine.run

import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logic.routine.usecase.RecordRoutineExecutionUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.preferences.AppPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
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
class RoutineRunEngineImplTest : BaseTest() {
  private val routineRepository = mockk<RoutineRepository>()
  private val recordUseCase = mockk<RecordRoutineExecutionUseCase>()
  private val appPreferences = mockk<AppPreferences>()
  private val foregroundNotifier = mockk<RoutineRunForegroundNotifier>(relaxed = true)
  private val tonePlayer = mockk<StepTonePlayer>(relaxed = true)

  private fun TestScope.createEngine(
    steps: List<RoutineStep>,
    autoAdvance: Boolean = true,
    hapticsEnabled: Boolean = false,
    soundAlerts: Boolean = false,
  ): RoutineRunEngineImpl {
    val routine = Routine(
      id = ROUTINE_ID,
      title = "Morning",
      steps = steps,
      autoAdvance = autoAdvance,
      soundAlertsEnabled = soundAlerts,
      createdAt = LocalDateTime.of(2026, 1, 1, 0, 0),
      updatedAt = LocalDateTime.of(2026, 1, 1, 0, 0),
    )
    coEvery { routineRepository.getById(ROUTINE_ID) } returns routine
    coEvery { routineRepository.getById(OTHER_ID) } returns null
    coEvery { recordUseCase(any(), any(), any(), any()) } returns mockk(relaxed = true)
    every { appPreferences.hapticsEnabled } returns hapticsEnabled

    val mainDispatcher = StandardTestDispatcher(testScheduler)
    val dispatcherProvider = mockk<DispatcherProvider>()
    every { dispatcherProvider.default() } returns Dispatchers.Unconfined
    every { dispatcherProvider.io() } returns Dispatchers.Unconfined
    every { dispatcherProvider.main() } returns mainDispatcher

    val engine = RoutineRunEngineImpl(
      dispatcherProvider = dispatcherProvider,
      routineRepository = routineRepository,
      recordRoutineExecutionUseCase = recordUseCase,
      appPreferences = appPreferences,
      foregroundNotifier = foregroundNotifier,
      tonePlayer = tonePlayer,
      currentTimeMillis = { testScheduler.currentTime },
    )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      try {
        awaitCancellation()
      } finally {
        engine.stop()
      }
    }
    return engine
  }

  private fun RoutineRunEngineImpl.running() = state.value as RoutineRunState.Running

  private fun TestScope.advanceSeconds(seconds: Int) {
    advanceTimeBy(seconds * 1000L)
    runCurrent()
  }

  private fun TestScope.startedEngine(
    steps: List<RoutineStep>,
    autoAdvance: Boolean = true,
  ): RoutineRunEngineImpl {
    val engine = createEngine(steps, autoAdvance)
    engine.start(ROUTINE_ID)
    runCurrent()
    return engine
  }

  private fun step(id: String, seconds: Int, order: Int) =
    RoutineStep(id = id, title = id, durationSeconds = seconds, order = order)

  @Test
  fun `countdown ticks down from the absolute deadline`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0), step("b", 60, 1)))
    assertEquals(60, engine.running().remainingSeconds)

    advanceSeconds(10)

    assertEquals(50, engine.running().remainingSeconds)
  }

  @Test
  fun `pause freezes remaining time and resume re-anchors the deadline`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0)))
    advanceSeconds(10)

    engine.pause()
    advanceSeconds(30)
    assertTrue(engine.running().isPaused)
    assertEquals(50, engine.running().remainingSeconds)

    engine.resume()
    advanceSeconds(5)

    assertFalse(engine.running().isPaused)
    assertEquals(45, engine.running().remainingSeconds)
  }

  @Test
  fun `add minute extends the remaining time`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0)))
    advanceSeconds(10)

    engine.addMinute()

    assertEquals(110, engine.running().remainingSeconds)
  }

  @Test
  fun `skip does not mark the step completed`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0)))

    engine.skip()
    runCurrent()

    assertTrue(engine.state.value is RoutineRunState.Finished)
    coVerify { recordUseCase(ROUTINE_ID, emptyList(), any(), 1) }
  }

  @Test
  fun `complete marks the step completed`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0), step("b", 60, 1)))

    engine.completeStep()
    assertEquals(1, engine.running().stepIndex)
    engine.skip()
    runCurrent()

    coVerify { recordUseCase(ROUTINE_ID, listOf("a"), any(), 2) }
  }

  @Test
  fun `expiry auto advances and marks the step completed`() = runTest {
    val engine = startedEngine(listOf(step("a", 5, 0), step("b", 60, 1)))

    advanceSeconds(6)

    assertEquals(1, engine.running().stepIndex)
    engine.completeStep()
    runCurrent()
    coVerify { recordUseCase(ROUTINE_ID, listOf("a", "b"), any(), 2) }
  }

  @Test
  fun `expiry does not advance when auto advance is off`() = runTest {
    val engine = startedEngine(listOf(step("a", 5, 0), step("b", 60, 1)), autoAdvance = false)

    advanceSeconds(10)

    assertEquals(0, engine.running().stepIndex)
    assertEquals(0, engine.running().remainingSeconds)
  }

  @Test
  fun `finishing shows the summary and records once`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0)))
    advanceSeconds(20)

    engine.completeStep()
    engine.completeStep()
    runCurrent()

    val finished = engine.state.value as RoutineRunState.Finished
    assertEquals(1, finished.completedCount)
    assertEquals(1, finished.totalCount)
    assertEquals(20, finished.totalTimeSeconds)
    coVerify(exactly = 1) { recordUseCase(any(), any(), any(), any()) }
  }

  @Test
  fun `stop mid run records silently once and returns to idle`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0), step("b", 60, 1)))

    engine.completeStep()
    engine.stop()
    engine.stop()
    runCurrent()

    assertEquals(RoutineRunState.Idle, engine.state.value)
    coVerify(exactly = 1) { recordUseCase(ROUTINE_ID, listOf("a"), any(), 2) }
    verify { foregroundNotifier.stop() }
  }

  @Test
  fun `untimed step has no countdown and ignores timer controls`() = runTest {
    val engine = startedEngine(listOf(step("a", 0, 0)))

    engine.pause()
    engine.addMinute()
    advanceSeconds(30)

    val running = engine.running()
    assertFalse(running.isTimed)
    assertFalse(running.isPaused)
    assertEquals(0, running.remainingSeconds)
    engine.completeStep()
    runCurrent()
    assertTrue(engine.state.value is RoutineRunState.Finished)
  }

  @Test
  fun `starting a run starts the foreground notifier and finishing stops it`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0)))
    verify(exactly = 1) { foregroundNotifier.start() }

    engine.completeStep()

    verify { foregroundNotifier.stop() }
  }

  @Test
  fun `starting the same routine again keeps the active run`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0), step("b", 60, 1)))
    engine.completeStep()

    engine.start(ROUTINE_ID)
    runCurrent()

    assertEquals(1, engine.running().stepIndex)
    verify(exactly = 1) { foregroundNotifier.start() }
  }

  @Test
  fun `starting another routine records the active run silently and replaces it`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0)))

    engine.start(OTHER_ID)
    runCurrent()

    coVerify(exactly = 1) { recordUseCase(ROUTINE_ID, emptyList(), any(), 1) }
    assertEquals(RoutineRunState.NotFound(OTHER_ID), engine.state.value)
  }

  @Test
  fun `unknown routine ends in not found without starting the notifier`() = runTest {
    val engine = createEngine(listOf(step("a", 60, 0)))

    engine.start(OTHER_ID)
    runCurrent()

    assertEquals(RoutineRunState.NotFound(OTHER_ID), engine.state.value)
    verify(exactly = 0) { foregroundNotifier.start() }
  }

  @Test
  fun `pause is ignored while loading and previous is ignored on the first step`() = runTest {
    val engine = createEngine(listOf(step("a", 60, 0), step("b", 60, 1)))
    engine.start(ROUTINE_ID)
    engine.pause()
    runCurrent()
    assertFalse(engine.running().isPaused)

    engine.previous()
    assertEquals(0, engine.running().stepIndex)
    engine.completeStep()
    engine.previous()
    assertEquals(0, engine.running().stepIndex)
  }

  @Test
  fun `step transition is emitted when haptics are enabled`() = runTest {
    val engine = createEngine(listOf(step("a", 60, 0), step("b", 60, 1)), hapticsEnabled = true)
    var transitions = 0
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { engine.stepTransitions.collect { transitions++ } }
    engine.start(ROUTINE_ID)
    runCurrent()

    engine.completeStep()

    assertEquals(1, transitions)
  }

  @Test
  fun `step transition is not emitted when haptics are disabled`() = runTest {
    val engine = createEngine(listOf(step("a", 60, 0), step("b", 60, 1)), hapticsEnabled = false)
    var transitions = 0
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { engine.stepTransitions.collect { transitions++ } }
    engine.start(ROUTINE_ID)
    runCurrent()

    engine.completeStep()

    assertEquals(0, transitions)
  }

  @Test
  fun `previous step moves back and re-anchors the countdown`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0), step("b", 60, 1)))
    engine.completeStep()
    advanceSeconds(20)
    assertEquals(40, engine.running().remainingSeconds)

    engine.previous()
    assertEquals(0, engine.running().stepIndex)
    assertEquals(60, engine.running().remainingSeconds)
    advanceSeconds(10)

    assertEquals(50, engine.running().remainingSeconds)
  }

  @Test
  fun `sound alert plays on a step transition only when enabled`() = runTest {
    val enabled = createEngine(listOf(step("a", 60, 0), step("b", 60, 1)), soundAlerts = true)
    enabled.start(ROUTINE_ID)
    runCurrent()
    enabled.completeStep()
    verify(exactly = 1) { tonePlayer.play() }
  }

  @Test
  fun `sound alert does not play when disabled`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0), step("b", 60, 1)))

    engine.completeStep()

    verify(exactly = 0) { tonePlayer.play() }
  }

  @Test
  fun `paused time is excluded from the total when skipping while paused`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0), step("b", 60, 1)))
    advanceSeconds(10)
    engine.pause()
    advanceSeconds(30)

    engine.skip()
    advanceSeconds(5)
    engine.completeStep()

    val finished = engine.state.value as RoutineRunState.Finished
    assertEquals(15, finished.totalTimeSeconds)
  }

  @Test
  fun `paused time is excluded from the total when going back while paused`() = runTest {
    val engine = startedEngine(listOf(step("a", 60, 0), step("b", 60, 1)))
    engine.completeStep()
    advanceSeconds(10)
    engine.pause()
    advanceSeconds(20)

    engine.previous()
    advanceSeconds(5)
    engine.completeStep()
    engine.completeStep()

    val finished = engine.state.value as RoutineRunState.Finished
    assertEquals(15, finished.totalTimeSeconds)
  }

  @Test
  fun `a stale owner releasing does not stop a run re-attached by a newer owner`() = runTest {
    val engine = createEngine(listOf(step("a", 60, 0)))
    val oldToken = engine.start(ROUTINE_ID)
    runCurrent()
    val newToken = engine.start(ROUTINE_ID)

    engine.release(oldToken)
    assertTrue(engine.state.value is RoutineRunState.Running)

    engine.release(newToken)
    assertEquals(RoutineRunState.Idle, engine.state.value)
  }

  @Test
  fun `a notifier that fails to start the service does not stop the run`() = runTest {
    every { foregroundNotifier.start() } throws IllegalStateException("not allowed")
    val engine = startedEngine(listOf(step("a", 60, 0)))

    advanceSeconds(10)

    assertEquals(50, engine.running().remainingSeconds)
  }

  private companion object {
    const val ROUTINE_ID = "routine-1"
    const val OTHER_ID = "routine-2"
  }
}

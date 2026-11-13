package com.github.naz013.appwidgets.routine

import com.github.naz013.common.TextProvider
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.feature.routine.run.RoutineRunEngine
import com.github.naz013.feature.routine.run.RoutineRunState
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.logic.routine.RoutineDurationCalculator
import com.github.naz013.repository.RoutineRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class RoutineAppWidgetViewModelTest {

  private val stamp = LocalDateTime.of(2026, 10, 1, 8, 0)
  private val prefsProvider = mockk<RoutineWidgetPrefsProvider>(relaxed = true)
  private val routineConfig = mockk<RoutineConfig>()
  private val routineRepository = mockk<RoutineRepository>()
  private val runEngine = mockk<RoutineRunEngine>()
  private val runState = MutableStateFlow<RoutineRunState>(RoutineRunState.Idle)
  private val textProvider = mockk<TextProvider>()
  private val viewModel = RoutineAppWidgetViewModel(
    prefsProvider,
    routineConfig,
    routineRepository,
    runEngine,
    RoutineDurationCalculator(),
    textProvider
  )

  @Before
  fun setUp() {
    every { prefsProvider.widgetId } returns 7
    every { prefsProvider.getRoutineId() } returns null
    every { routineConfig.isEnabled } returns true
    every { runEngine.state } returns runState
    every { textProvider.getString(any()) } returns "status"
    every { textProvider.getString(any(), *anyVararg()) } answers {
      "${(args[1] as Array<*>).joinToString("/")}"
    }
  }

  private fun routine(
    id: String,
    title: String = "Routine $id",
    recurring: Boolean = true,
    steps: List<RoutineStep> = listOf(RoutineStep(id = "$id-1", durationSeconds = 600)),
    color: Int = 0,
    icon: Int? = null
  ) = Routine(
    id = id,
    title = title,
    steps = steps,
    color = color,
    icon = icon,
    recurrence = if (recurring) RecurrenceRule.Daily() else null,
    createdAt = stamp,
    updatedAt = stamp
  )

  @Test
  fun `returns locked state without reading routines when routines are disabled`() = runTest {
    every { routineConfig.isEnabled } returns false

    val state = viewModel.getState()

    assertFalse(state.isUnlocked)
    assertNull(state.item)
    coVerify(exactly = 0) { routineRepository.getAll() }
    coVerify(exactly = 0) { routineRepository.getById(any()) }
  }

  @Test
  fun `shows the configured routine with step count and duration`() = runTest {
    every { prefsProvider.getRoutineId() } returns "r-1"
    coEvery { routineRepository.getById("r-1") } returns routine(
      id = "r-1",
      steps = listOf(RoutineStep(id = "a", durationSeconds = 600), RoutineStep(id = "b", durationSeconds = 900)),
      icon = 0
    )

    val item = viewModel.getState().item

    assertNotNull(item)
    assertEquals("r-1", item!!.id)
    assertEquals("2", item.stepCountLabel)
    assertEquals("25m", item.durationLabel)
    assertNotNull(item.iconRes)
  }

  @Test
  fun `returns no item when the configured routine was deleted`() = runTest {
    every { prefsProvider.getRoutineId() } returns "gone"
    coEvery { routineRepository.getById("gone") } returns null

    assertNull(viewModel.getState().item)
  }

  @Test
  fun `next scheduled picks the recurring routine with the earliest step time`() = runTest {
    coEvery { routineRepository.getAll() } returns listOf(
      routine("late", steps = listOf(RoutineStep(id = "a", scheduledTime = "20:00"))),
      routine("early", steps = listOf(RoutineStep(id = "b", scheduledTime = "07:30"))),
      routine("on-demand", recurring = false, steps = listOf(RoutineStep(id = "c", scheduledTime = "05:00")))
    )

    assertEquals("early", viewModel.getState().item?.id)
  }

  @Test
  fun `next scheduled prefers a routine that is not done yet`() = runTest {
    coEvery { routineRepository.getAll() } returns listOf(
      routine("done", steps = listOf(RoutineStep(id = "a", scheduledTime = "06:00", isCompleted = true))),
      routine("pending", steps = listOf(RoutineStep(id = "b", scheduledTime = "21:00")))
    )

    assertEquals("pending", viewModel.getState().item?.id)
  }

  @Test
  fun `next scheduled returns no item when there are no recurring routines`() = runTest {
    coEvery { routineRepository.getAll() } returns listOf(routine("a", recurring = false))

    assertNull(viewModel.getState().item)
  }

  @Test
  fun `marks the item as running while the run engine runs that routine`() = runTest {
    every { prefsProvider.getRoutineId() } returns "r-1"
    coEvery { routineRepository.getById("r-1") } returns routine("r-1")
    runState.value = RoutineRunState.Running(
      routineId = "r-1",
      routineTitle = "Routine r-1",
      stepIndex = 0,
      stepCount = 1,
      stepTitle = "Step",
      scheduledTimeLabel = null,
      isTimed = true,
      remainingSeconds = 60,
      stepTotalSeconds = 60,
      isPaused = false,
      stepEndsAtMillis = null,
      completedCount = 0
    )

    assertTrue(viewModel.getState().item!!.isRunning)
  }

  @Test
  fun `is not running when the engine runs a different routine`() = runTest {
    every { prefsProvider.getRoutineId() } returns "r-1"
    coEvery { routineRepository.getById("r-1") } returns routine("r-1")
    runState.value = RoutineRunState.Loading("other")

    assertFalse(viewModel.getState().item!!.isRunning)
  }

  @Test
  fun `status shows completed step progress when some steps are done`() = runTest {
    every { prefsProvider.getRoutineId() } returns "r-1"
    coEvery { routineRepository.getById("r-1") } returns routine(
      id = "r-1",
      steps = listOf(RoutineStep(id = "a", isCompleted = true), RoutineStep(id = "b"))
    )

    assertEquals("1/2", viewModel.getState().item!!.statusLabel)
  }
}

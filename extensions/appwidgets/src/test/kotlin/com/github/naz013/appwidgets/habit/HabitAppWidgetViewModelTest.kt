package com.github.naz013.appwidgets.habit

import com.github.naz013.common.TextProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.logic.habit.HabitWithProgress
import com.github.naz013.logic.habit.usecase.ObserveHabitsWithProgressUseCase
import com.github.naz013.logic.routine.HabitConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class HabitAppWidgetViewModelTest {

  private val stamp = LocalDateTime.of(2026, 10, 1, 8, 0)
  private val prefsProvider = mockk<HabitWidgetPrefsProvider>(relaxed = true)
  private val habitConfig = mockk<HabitConfig>()
  private val observeUseCase = mockk<ObserveHabitsWithProgressUseCase>()
  private val textProvider = mockk<TextProvider>()
  private val viewModel = HabitAppWidgetViewModel(prefsProvider, habitConfig, observeUseCase, textProvider)

  @Before
  fun setUp() {
    every { prefsProvider.widgetId } returns 7
    every { prefsProvider.getBackground() } returns 3
    every { habitConfig.isEnabled } returns true
    every { textProvider.getString(any(), *anyVararg()) } answers { "week ${(args[1] as Array<*>)[1]}" }
  }

  private fun progress(
    id: String,
    pinned: Boolean = false,
    frequency: HabitFrequency = HabitFrequency.DAILY,
    target: Int = 1,
    periodCount: Int = 0
  ) = HabitWithProgress(
    habit = Habit(
      id = id,
      name = "Habit $id",
      frequency = frequency,
      targetCount = target,
      isPinned = pinned,
      createdAt = stamp,
      updatedAt = stamp
    ),
    todayCount = periodCount,
    periodCount = periodCount,
    streak = 0
  )

  @Test
  fun `returns locked state without reading habits when habits are unavailable`() = runTest {
    every { habitConfig.isEnabled } returns false

    val state = viewModel.getState()

    assertFalse(state.isUnlocked)
    assertTrue(state.items.isEmpty())
    assertEquals(7, state.widgetId)
    assertEquals(3, state.backgroundColor)
    verify(exactly = 0) { observeUseCase() }
  }

  @Test
  fun `lists pinned habits first and keeps the rest in order`() = runTest {
    every { observeUseCase() } returns flowOf(listOf(progress("a"), progress("b", pinned = true), progress("c")))

    val state = viewModel.getState()

    assertTrue(state.isUnlocked)
    assertEquals(listOf("b", "a", "c"), state.items.map { it.id })
  }

  @Test
  fun `shows daily progress against the target count`() = runTest {
    every { observeUseCase() } returns flowOf(listOf(progress("a", target = 3, periodCount = 2)))

    val item = viewModel.getState().items.single()

    assertEquals("2/3", item.progressLabel)
    assertFalse(item.isDone)
  }

  @Test
  fun `marks a habit done once the target is reached`() = runTest {
    every { observeUseCase() } returns flowOf(listOf(progress("a", target = 2, periodCount = 2)))

    assertTrue(viewModel.getState().items.single().isDone)
  }

  @Test
  fun `uses the weekly progress text for weekly habits`() = runTest {
    every { observeUseCase() } returns flowOf(
      listOf(progress("a", frequency = HabitFrequency.WEEKLY, target = 4, periodCount = 1))
    )

    assertEquals("week 4", viewModel.getState().items.single().progressLabel)
  }
}

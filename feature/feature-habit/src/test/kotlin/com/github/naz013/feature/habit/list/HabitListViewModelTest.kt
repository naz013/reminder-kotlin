package com.github.naz013.feature.habit.list

import com.github.naz013.common.TextProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.logic.habit.HabitWithProgress
import com.github.naz013.logic.habit.usecase.CheckInHabitUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitsWithProgressUseCase
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class HabitListViewModelTest : BaseTest() {
  private val stamp = LocalDateTime.of(2026, 10, 1, 8, 0)
  private val habits = MutableStateFlow<List<HabitWithProgress>>(emptyList())
  private val observeUseCase = mockk<ObserveHabitsWithProgressUseCase> {
    every { this@mockk.invoke() } returns habits
  }
  private val checkInUseCase = mockk<CheckInHabitUseCase>(relaxed = true)
  private val textProvider = mockk<TextProvider> {
    every { getString(any(), *anyVararg()) } answers { "text-" + (args[1] as Array<*>).joinToString("-") }
  }

  private fun createViewModel() = HabitListViewModel(
    dispatcherProvider = mockDispatcherProvider(),
    observeHabitsWithProgressUseCase = observeUseCase,
    checkInHabitUseCase = checkInUseCase,
    textProvider = textProvider,
  )

  private fun progress(
    frequency: HabitFrequency = HabitFrequency.DAILY,
    target: Int = 1,
    todayCount: Int = 0,
    periodCount: Int = todayCount,
    streak: Int = 0,
  ) = HabitWithProgress(
    habit = Habit(
      id = "h-1",
      name = "Run",
      frequency = frequency,
      targetCount = target,
      createdAt = stamp,
      updatedAt = stamp
    ),
    todayCount = todayCount,
    periodCount = periodCount,
    streak = streak
  )

  private fun HabitListViewModel.readyItems(): List<UiHabitListItem> {
    val listState = state.value.listState
    assertTrue(listState is HabitListDisplayState.Ready)
    return (listState as HabitListDisplayState.Ready).habits
  }

  @Test
  fun `shows the empty state when there are no habits`() {
    val viewModel = createViewModel()

    habits.value = emptyList()

    assertEquals(HabitListDisplayState.Empty, viewModel.currentListState())
  }

  @Test
  fun `maps a daily habit to a today over target label with no week label`() {
    habits.value = listOf(progress(target = 3, todayCount = 2, streak = 4))
    val viewModel = createViewModel()

    val item = viewModel.readyItems().single()

    assertEquals("2/3", item.todayProgressLabel)
    assertNull(item.weekProgressLabel)
    assertEquals("4", item.streakLabel)
  }

  @Test
  fun `maps a weekly habit to a week progress label`() {
    habits.value = listOf(progress(frequency = HabitFrequency.WEEKLY, target = 3, todayCount = 1, periodCount = 2))
    val viewModel = createViewModel()

    val item = viewModel.readyItems().single()

    assertEquals("1", item.todayProgressLabel)
    assertEquals("text-2-3", item.weekProgressLabel)
  }

  @Test
  fun `marks the target as reached when the period count meets it`() {
    habits.value = listOf(progress(target = 2, todayCount = 2))
    val viewModel = createViewModel()

    assertTrue(viewModel.readyItems().single().isTargetReached)
  }

  @Test
  fun `check-in delegates the habit progress to the check-in use case`() {
    val item = progress(target = 3, todayCount = 1)
    habits.value = listOf(item)
    val viewModel = createViewModel()

    viewModel.onCheckInClick("h-1")

    coVerify(exactly = 1) { checkInUseCase(item) }
  }

  @Test
  fun `ignores a check-in for an unknown habit`() {
    val viewModel = createViewModel()

    viewModel.onCheckInClick("missing")

    coVerify(exactly = 0) { checkInUseCase(any()) }
  }

  @Test
  fun `opens the preview when a habit card is clicked`() {
    val viewModel = createViewModel()
    var event: HabitListViewModel.NavigationEvent? = null
    viewModel.navigationEvent.observeForever { event = it.getContentIfNotHandled() }

    viewModel.onHabitClick("h-1")

    assertEquals(HabitListViewModel.NavigationEvent.OpenPreview("h-1"), event)
  }

  private fun HabitListViewModel.currentListState() = state.value.listState
}

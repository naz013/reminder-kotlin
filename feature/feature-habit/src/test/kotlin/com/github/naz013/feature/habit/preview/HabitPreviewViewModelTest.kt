package com.github.naz013.feature.habit.preview

import com.github.naz013.common.TextProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.logic.habit.HabitDay
import com.github.naz013.logic.habit.HabitDetails
import com.github.naz013.logic.habit.usecase.CheckInHabitUseCase
import com.github.naz013.logic.habit.usecase.DeleteHabitUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitDetailsUseCase
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class HabitPreviewViewModelTest : BaseTest() {
  private val stamp = LocalDateTime.of(2026, 10, 1, 8, 0)
  private val details = MutableStateFlow<HabitDetails?>(null)
  private val observeUseCase = mockk<ObserveHabitDetailsUseCase> {
    every { this@mockk.invoke("h-1", any()) } returns details
  }
  private val checkInUseCase = mockk<CheckInHabitUseCase>(relaxed = true)
  private val deleteUseCase = mockk<DeleteHabitUseCase>(relaxed = true)
  private val textProvider = mockk<TextProvider> {
    every { getString(any(), *anyVararg()) } answers { "text-" + (args[1] as Array<*>).joinToString("-") }
    every { getString(any<Int>()) } returns "text"
  }
  private val events = mutableListOf<HabitPreviewViewModel.NavigationEvent>()

  private fun createViewModel(): HabitPreviewViewModel {
    val viewModel = HabitPreviewViewModel(
      id = "h-1",
      dispatcherProvider = mockDispatcherProvider(),
      observeHabitDetailsUseCase = observeUseCase,
      checkInHabitUseCase = checkInUseCase,
      deleteHabitUseCase = deleteUseCase,
      textProvider = textProvider,
    )
    viewModel.navigationEvent.observeForever { event -> event.getContentIfNotHandled()?.let { events += it } }
    return viewModel
  }

  private fun habitDetails(
    frequency: HabitFrequency = HabitFrequency.DAILY,
    target: Int = 1,
    todayCount: Int = 0,
    streak: Int = 2,
  ) = HabitDetails(
    habit = Habit(
      id = "h-1",
      name = "Run",
      description = "Every morning",
      frequency = frequency,
      targetCount = target,
      createdAt = stamp,
      updatedAt = stamp
    ),
    todayCount = todayCount,
    periodCount = todayCount,
    streak = streak,
    longestStreak = 5,
    totalCheckIns = 12,
    days = (0 until 9).map { HabitDay(LocalDate.of(2026, 9, 28).plusDays(it.toLong()), count = 1, level = it % 5) }
  )

  private fun HabitPreviewViewModel.ready(): HabitPreviewContent.Ready {
    val content = state.value.content
    assertTrue(content is HabitPreviewContent.Ready)
    return content as HabitPreviewContent.Ready
  }

  @Test
  fun `stays loading until the habit details arrive`() {
    val viewModel = createViewModel()

    assertEquals(HabitPreviewContent.Loading, viewModel.state.value.content)
  }

  @Test
  fun `maps a daily habit to a today over target label and the stats`() {
    details.value = habitDetails(target = 3, todayCount = 2)
    val viewModel = createViewModel()

    val content = viewModel.ready()

    assertEquals("2/3", content.todayProgressLabel)
    assertNull(content.weekProgressLabel)
    assertEquals(listOf("2", "5", "12"), content.stats.map { it.value })
    assertEquals("Every morning", content.description)
  }

  @Test
  fun `maps a weekly habit to a week progress label`() {
    details.value = habitDetails(frequency = HabitFrequency.WEEKLY, target = 3, todayCount = 1)
    val viewModel = createViewModel()

    val content = viewModel.ready()

    assertEquals("1", content.todayProgressLabel)
    assertEquals("text-1-3", content.weekProgressLabel)
  }

  @Test
  fun `splits the days into Monday-start weeks and labels only the first column of a month`() {
    details.value = habitDetails()
    val viewModel = createViewModel()

    val weeks = viewModel.ready().activityWeeks

    assertEquals(listOf(7, 2), weeks.map { it.levels.size })
    assertTrue(weeks.first().monthLabel != null)
    assertEquals(listOf(0, 1, 2, 3, 4, 0, 1), weeks.first().levels)
  }

  @Test
  fun `check-in hands the current progress to the check-in use case`() {
    val current = habitDetails(target = 3, todayCount = 1)
    details.value = current
    val viewModel = createViewModel()

    viewModel.onCheckInClick()

    coVerify(exactly = 1) { checkInUseCase(current.progress) }
  }

  @Test
  fun `ignores a check-in before the habit is loaded`() {
    val viewModel = createViewModel()

    viewModel.onCheckInClick()

    coVerify(exactly = 0) { checkInUseCase(any()) }
  }

  @Test
  fun `edit opens the editor for the habit`() {
    details.value = habitDetails()
    val viewModel = createViewModel()

    viewModel.onEditClick()

    assertEquals(listOf<HabitPreviewViewModel.NavigationEvent>(HabitPreviewViewModel.NavigationEvent.OpenEditor("h-1")), events)
  }

  @Test
  fun `delete asks for confirmation and deletes only once confirmed`() {
    details.value = habitDetails()
    val viewModel = createViewModel()

    viewModel.onDeleteClick()
    assertTrue(viewModel.state.value.isDeleteConfirmationVisible)
    coVerify(exactly = 0) { deleteUseCase(any()) }

    viewModel.onDeleteConfirmed()

    assertFalse(viewModel.state.value.isDeleteConfirmationVisible)
    coVerify(exactly = 1) { deleteUseCase("h-1") }
    assertEquals(listOf<HabitPreviewViewModel.NavigationEvent>(HabitPreviewViewModel.NavigationEvent.Back), events)
  }

  @Test
  fun `dismissing the delete confirmation keeps the habit`() {
    details.value = habitDetails()
    val viewModel = createViewModel()

    viewModel.onDeleteClick()
    viewModel.onDeleteDismissed()

    assertFalse(viewModel.state.value.isDeleteConfirmationVisible)
    coVerify(exactly = 0) { deleteUseCase(any()) }
  }

  @Test
  fun `goes back when the habit disappears after being shown`() {
    details.value = habitDetails()
    createViewModel()

    details.value = null

    assertEquals(listOf<HabitPreviewViewModel.NavigationEvent>(HabitPreviewViewModel.NavigationEvent.Back), events)
  }

  @Test
  fun `emits a single back when our own delete removes the habit`() {
    details.value = habitDetails()
    val viewModel = createViewModel()

    viewModel.onDeleteConfirmed()
    details.value = null

    assertEquals(listOf<HabitPreviewViewModel.NavigationEvent>(HabitPreviewViewModel.NavigationEvent.Back), events)
  }
}

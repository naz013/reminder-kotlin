package com.github.naz013.feature.routine.edit

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.logic.routine.usecase.DeleteRoutineUseCase
import com.github.naz013.logic.routine.usecase.SaveRoutineUseCase
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.preferences.AppPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

/** [RoutineEditViewModel.state] is shared via `stateInWhileSubscribed` (`SharingStarted
 * .WhileSubscribed`), so its `.value` only reflects live updates once something has actually
 * subscribed - reading `.value` cold (as production `collectAsState()` callers never do) would
 * silently return the frozen initial [RoutineEditState]. Every assertion here goes through
 * [kotlinx.coroutines.flow.first] instead, which subscribes and immediately gets the current
 * value from the always-live backing `MutableStateFlow`. */
class RoutineEditViewModelTest : BaseTest() {
  private val routineRepository = mockk<RoutineRepository>()
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val toggleTagAssignmentUseCase = mockk<ToggleTagAssignmentUseCase>(relaxed = true)
  private val saveRoutineUseCase = mockk<SaveRoutineUseCase>()
  private val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>(relaxed = true)
  private val nowDateTimeProvider = mockk<NowDateTimeProvider>()
  private val appPreferences = mockk<AppPreferences>()

  private val now = LocalDateTime.of(2026, 7, 22, 9, 0)

  @Before
  override fun setUp() {
    super.setUp()
    every { tagRepository.observeAll() } returns flowOf(emptyList())
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
    every { nowDateTimeProvider.nowDateTime() } returns now
    every { appPreferences.hapticsEnabled } returns true
    coEvery { routineRepository.getById(any()) } returns null
    coEvery { saveRoutineUseCase(any()) } answers { firstArg() }
  }

  private fun createViewModel(id: String? = null): RoutineEditViewModel =
    RoutineEditViewModel(
      id = id,
      dispatcherProvider = mockDispatcherProvider(),
      routineRepository = routineRepository,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      tagChipStateAdapter = mockk(relaxed = true),
      toggleTagAssignmentUseCase = toggleTagAssignmentUseCase,
      saveRoutineUseCase = saveRoutineUseCase,
      deleteRoutineUseCase = deleteRoutineUseCase,
      nowDateTimeProvider = nowDateTimeProvider,
      appPreferences = appPreferences,
    )

  @Test
  fun `canSave is false for a new routine with no title and no steps`() = runTest {
    val viewModel = createViewModel()

    assertFalse(viewModel.state.first().canSave)
  }

  @Test
  fun `save is rejected when steps are empty, even with a title`() = runTest {
    val viewModel = createViewModel()

    viewModel.onTitleChange("Morning routine")
    viewModel.onSaveClick()

    assertFalse(viewModel.state.first().canSave)
    coVerify(exactly = 0) { saveRoutineUseCase(any()) }
  }

  @Test
  fun `save is rejected when title is blank, even with steps`() = runTest {
    val viewModel = createViewModel()

    viewModel.onAddStepClick()
    viewModel.onSaveClick()

    assertFalse(viewModel.state.first().canSave)
    coVerify(exactly = 0) { saveRoutineUseCase(any()) }
  }

  @Test
  fun `save proceeds once both a title and at least one step are present`() = runTest {
    val viewModel = createViewModel()

    viewModel.onTitleChange("Morning routine")
    viewModel.onAddStepClick()
    assertTrue(viewModel.state.first().canSave)

    viewModel.onSaveClick()

    coVerify(exactly = 1) { saveRoutineUseCase(match { it.title == "Morning routine" && it.steps.size == 1 }) }
  }

  @Test
  fun `removing the last step disables save again`() = runTest {
    val viewModel = createViewModel()
    viewModel.onTitleChange("Morning routine")
    viewModel.onAddStepClick()
    val stepId = viewModel.state.first().steps.single().id

    viewModel.onRemoveStepClick(stepId)

    assertFalse(viewModel.state.first().canSave)
  }

  @Test
  fun `new routine with recurrence enabled anchors lastResetAt to the save time`() = runTest {
    val viewModel = createViewModel()

    viewModel.onTitleChange("Morning routine")
    viewModel.onAddStepClick()
    viewModel.onRecurrenceOptionChange(RoutineRecurrenceOption.Daily)
    viewModel.onSaveClick()

    coVerify(exactly = 1) {
      saveRoutineUseCase(match { it.recurrence == RecurrenceRule.Daily() && it.lastResetAt == now })
    }
  }

  @Test
  fun `on-demand routine is saved with no lastResetAt anchor`() = runTest {
    val viewModel = createViewModel()

    viewModel.onTitleChange("Post-workout stretch")
    viewModel.onAddStepClick()
    viewModel.onSaveClick()

    coVerify(exactly = 1) {
      saveRoutineUseCase(match { it.recurrence == null && it.lastResetAt == null })
    }
  }

  @Test
  fun `resaving an already-recurring routine keeps its existing lastResetAt anchor`() = runTest {
    val midCycleResetAt = LocalDateTime.of(2026, 7, 20, 8, 0)
    val existing = Routine(
      id = "id-1",
      title = "Morning routine",
      steps = listOf(RoutineStep(id = "s1", title = "Meditate")),
      recurrence = RecurrenceRule.Daily(),
      lastResetAt = midCycleResetAt,
      createdAt = midCycleResetAt,
      updatedAt = midCycleResetAt,
    )
    coEvery { routineRepository.getById("id-1") } returns existing

    val viewModel = createViewModel(id = "id-1")
    viewModel.onDescriptionChange("Updated description")
    viewModel.onSaveClick()

    coVerify(exactly = 1) { saveRoutineUseCase(match { it.lastResetAt == midCycleResetAt }) }
  }

  @Test
  fun `turning recurrence off clears the lastResetAt anchor`() = runTest {
    val midCycleResetAt = LocalDateTime.of(2026, 7, 20, 8, 0)
    val existing = Routine(
      id = "id-2",
      title = "Morning routine",
      steps = listOf(RoutineStep(id = "s1", title = "Meditate")),
      recurrence = RecurrenceRule.Daily(),
      lastResetAt = midCycleResetAt,
      createdAt = midCycleResetAt,
      updatedAt = midCycleResetAt,
    )
    coEvery { routineRepository.getById("id-2") } returns existing

    val viewModel = createViewModel(id = "id-2")
    assertEquals(RoutineRecurrenceOption.Daily, viewModel.state.first().recurrenceOption)
    viewModel.onRecurrenceOptionChange(RoutineRecurrenceOption.None)
    viewModel.onSaveClick()

    coVerify(exactly = 1) { saveRoutineUseCase(match { it.recurrence == null && it.lastResetAt == null }) }
  }

  @Test
  fun `turning recurrence on for a previously on-demand routine starts a fresh cycle`() = runTest {
    val existing = Routine(
      id = "id-3",
      title = "Post-workout stretch",
      steps = listOf(RoutineStep(id = "s1", title = "Stretch")),
      recurrence = null,
      lastResetAt = null,
      createdAt = now,
      updatedAt = now,
    )
    coEvery { routineRepository.getById("id-3") } returns existing

    val viewModel = createViewModel(id = "id-3")
    viewModel.onRecurrenceOptionChange(RoutineRecurrenceOption.Daily)
    viewModel.onSaveClick()

    coVerify(exactly = 1) { saveRoutineUseCase(match { it.recurrence != null && it.lastResetAt == now }) }
  }

  @Test
  fun `weekly recurrence with no weekdays selected cannot be saved`() = runTest {
    val viewModel = createViewModel()

    viewModel.onTitleChange("Morning routine")
    viewModel.onAddStepClick()
    viewModel.onRecurrenceOptionChange(RoutineRecurrenceOption.Weekly())

    assertFalse(viewModel.state.first().canSave)
    viewModel.onSaveClick()
    coVerify(exactly = 0) { saveRoutineUseCase(any()) }
  }

  @Test
  fun `weekly recurrence saves the selected weekdays`() = runTest {
    val viewModel = createViewModel()

    viewModel.onTitleChange("Morning routine")
    viewModel.onAddStepClick()
    viewModel.onRecurrenceOptionChange(RoutineRecurrenceOption.Weekly(weekdays = setOf(1, 3, 5)))
    assertTrue(viewModel.state.first().canSave)
    viewModel.onSaveClick()

    coVerify(exactly = 1) {
      saveRoutineUseCase(match { it.recurrence == RecurrenceRule.Weekly(weekdays = listOf(1, 3, 5)) })
    }
  }

  @Test
  fun `monthly recurrence saves the selected day of month`() = runTest {
    val viewModel = createViewModel()

    viewModel.onTitleChange("Morning routine")
    viewModel.onAddStepClick()
    viewModel.onRecurrenceOptionChange(RoutineRecurrenceOption.Monthly(dayOfMonth = 15))
    viewModel.onSaveClick()

    coVerify(exactly = 1) {
      saveRoutineUseCase(match { it.recurrence == RecurrenceRule.Monthly(dayOfMonth = 15) })
    }
  }

  @Test
  fun `load maps a persisted weekly recurrence back into the weekly option`() = runTest {
    val existing = Routine(
      id = "id-5",
      title = "Gym",
      steps = listOf(RoutineStep(id = "s1", title = "Warm up")),
      recurrence = RecurrenceRule.Weekly(weekdays = listOf(2, 4)),
      lastResetAt = now,
      createdAt = now,
      updatedAt = now,
    )
    coEvery { routineRepository.getById("id-5") } returns existing

    val viewModel = createViewModel(id = "id-5")

    assertEquals(RoutineRecurrenceOption.Weekly(weekdays = setOf(2, 4)), viewModel.state.first().recurrenceOption)
  }

  @Test
  fun `load populates canSave from the persisted routine's title and steps`() = runTest {
    val existing = Routine(
      id = "id-4",
      title = "Evening routine",
      steps = listOf(RoutineStep(id = "s1", title = "Read")),
      createdAt = now,
      updatedAt = now,
    )
    coEvery { routineRepository.getById("id-4") } returns existing

    val viewModel = createViewModel(id = "id-4")
    val state = viewModel.state.first()

    assertTrue(state.canSave)
    assertEquals(1, state.steps.size)
    assertEquals("Evening routine", state.title)
  }
}

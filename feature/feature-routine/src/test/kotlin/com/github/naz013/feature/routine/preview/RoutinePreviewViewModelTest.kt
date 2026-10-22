package com.github.naz013.feature.routine.preview

import com.github.naz013.logic.routine.usecase.ResetRoutineStepsUseCase
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoutinePreviewViewModelTest : BaseTest() {
  private val routineRepository = mockk<RoutineRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val resetRoutineStepsUseCase = mockk<ResetRoutineStepsUseCase>()

  @Before
  override fun setUp() {
    super.setUp()
    every { routineRepository.observeById(any()) } returns flowOf(null)
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
    coEvery { resetRoutineStepsUseCase(any()) } returns null
  }

  private fun createViewModel(): RoutinePreviewViewModel = RoutinePreviewViewModel(
    id = ROUTINE_ID,
    dispatcherProvider = mockDispatcherProvider(),
    routineRepository = routineRepository,
    saveRoutineUseCase = mockk(relaxed = true),
    toggleRoutinePinUseCase = mockk(relaxed = true),
    resetRoutineStepsUseCase = resetRoutineStepsUseCase,
    deleteRoutineUseCase = mockk(relaxed = true),
    routineRecurrenceResetUseCase = mockk(relaxed = true),
    routineDurationCalculator = mockk(relaxed = true),
    textProvider = mockk(relaxed = true),
    tagAssignmentRepository = tagAssignmentRepository,
    tagChipStateAdapter = mockk(relaxed = true),
  )

  @Test
  fun `reset click shows the confirmation without resetting`() = runTest {
    val viewModel = createViewModel()

    viewModel.onResetStepsClick()

    assertTrue(viewModel.showResetConfirmation.first())
    coVerify(exactly = 0) { resetRoutineStepsUseCase(any()) }
  }

  @Test
  fun `confirming resets the steps and hides the confirmation`() = runTest {
    val viewModel = createViewModel()
    viewModel.onResetStepsClick()

    viewModel.onResetStepsConfirmed()

    assertFalse(viewModel.showResetConfirmation.first())
    coVerify(exactly = 1) { resetRoutineStepsUseCase(ROUTINE_ID) }
  }

  @Test
  fun `dismissing hides the confirmation without resetting`() = runTest {
    val viewModel = createViewModel()
    viewModel.onResetStepsClick()

    viewModel.onResetStepsDismissed()

    assertFalse(viewModel.showResetConfirmation.first())
    coVerify(exactly = 0) { resetRoutineStepsUseCase(any()) }
  }

  private companion object {
    const val ROUTINE_ID = "routine-1"
  }
}

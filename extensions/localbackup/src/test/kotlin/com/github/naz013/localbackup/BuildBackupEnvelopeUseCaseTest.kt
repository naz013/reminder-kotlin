package com.github.naz013.localbackup

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RoutineExecutionRepository
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class BuildBackupEnvelopeUseCaseTest {

  private val reminderV2Repository = mockk<ReminderV2Repository>(relaxed = true)
  private val groupV2Repository = mockk<GroupV2Repository>(relaxed = true)
  private val birthdayRepository = mockk<BirthdayRepository>(relaxed = true)
  private val placeRepository = mockk<PlaceRepository>(relaxed = true)
  private val recurPresetRepository = mockk<RecurPresetRepository>(relaxed = true)
  private val tagRepository = mockk<TagRepository>(relaxed = true)
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>(relaxed = true)
  private val routineRepository = mockk<RoutineRepository>(relaxed = true)
  private val routineExecutionRepository = mockk<RoutineExecutionRepository>(relaxed = true)
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>(relaxed = true)
  private val workflowTemplateRepository = mockk<WorkflowTemplateRepository>(relaxed = true)

  private lateinit var useCase: BuildBackupEnvelopeUseCase

  @Before
  fun setUp() {
    useCase = BuildBackupEnvelopeUseCase(
      reminderV2Repository = reminderV2Repository,
      groupV2Repository = groupV2Repository,
      birthdayRepository = birthdayRepository,
      placeRepository = placeRepository,
      recurPresetRepository = recurPresetRepository,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      routineRepository = routineRepository,
      routineExecutionRepository = routineExecutionRepository,
      workflowRuleRepository = workflowRuleRepository,
      workflowTemplateRepository = workflowTemplateRepository
    )
  }

  private fun reminder(id: String) = ReminderV2(
    uuId = id,
    summary = "Take pills",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0))
  )

  @Test
  fun `reads every repository`() = runTest {
    useCase()

    coVerify { reminderV2Repository.getAll() }
    coVerify { groupV2Repository.getAll() }
    coVerify { birthdayRepository.getAll() }
    coVerify { placeRepository.getAll() }
    coVerify { recurPresetRepository.getAll() }
    coVerify { tagRepository.getAll() }
    coVerify { tagAssignmentRepository.getAll() }
    coVerify { routineRepository.getAll() }
    coVerify { routineExecutionRepository.getAll() }
    coVerify { workflowRuleRepository.getAll() }
    coVerify { workflowTemplateRepository.getAll() }
  }

  @Test
  fun `excludes reminders flagged offline-only`() = runTest {
    coEvery { reminderV2Repository.getAll() } returns listOf(
      reminder("r1"),
      reminder("r2").copy(offlineOnly = true),
    )

    val envelope = useCase()

    assertEquals(1, envelope.reminders.size)
    assertEquals("r1", envelope.reminders.single().uuId)
  }
}

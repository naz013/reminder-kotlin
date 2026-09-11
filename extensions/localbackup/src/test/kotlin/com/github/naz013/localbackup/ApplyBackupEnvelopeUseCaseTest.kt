package com.github.naz013.localbackup

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.localbackup.archive.BackupEnvelope
import com.github.naz013.logic.birthday.CalculateBirthdayOccurrencesUseCase
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
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
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ApplyBackupEnvelopeUseCaseTest {

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
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val calculateBirthdayOccurrencesUseCase = mockk<CalculateBirthdayOccurrencesUseCase>(relaxed = true)

  private lateinit var useCase: ApplyBackupEnvelopeUseCase

  @Before
  fun setUp() {
    useCase = ApplyBackupEnvelopeUseCase(
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
      workflowTemplateRepository = workflowTemplateRepository,
      activateReminderUseCase = activateReminderUseCase,
      calculateBirthdayOccurrencesUseCase = calculateBirthdayOccurrencesUseCase
    )
  }

  @Test
  fun `upserts reminders and groups and returns matching counts`() = runTest {
    val envelope = BackupEnvelope(
      reminders = listOf(
        ReminderV2(
          uuId = "r1",
          summary = "Take pills",
          schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0))
        )
      ),
      groups = listOf(GroupV2(uuId = "g1", title = "Work", createdAt = LocalDateTime.now(), syncState = SyncState.Synced))
    )

    val summary = useCase(envelope)

    assertEquals(1, summary.remindersImported)
    assertEquals(1, summary.groupsImported)
    coVerify { reminderV2Repository.save(match { it.uuId == "r1" }) }
    coVerify { groupV2Repository.save(match { it.uuId == "g1" }) }
  }

  @Test
  fun `replaces tag assignments instead of upserting them one by one`() = runTest {
    val envelope = BackupEnvelope(
      tags = listOf(Tag(id = "t1", name = "Work", color = 1, syncState = SyncState.Synced)),
      tagAssignments = listOf(TagAssignment(tagId = "t1", itemId = "r1", itemType = TaggedItemType.REMINDER))
    )

    val summary = useCase(envelope)

    assertEquals(1, summary.tagsImported)
    assertEquals(1, summary.tagAssignmentsImported)
    coVerify { tagRepository.save(match { it.id == "t1" }) }
    coVerify { tagAssignmentRepository.replaceAll(match { it.size == 1 && it[0].tagId == "t1" }) }
  }

  @Test
  fun `upserts routines, workflow rules and templates`() = runTest {
    val envelope = BackupEnvelope(
      routines = listOf(Routine(id = "o1", title = "Morning", createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())),
      routineExecutions = listOf(
        RoutineExecutionRecord(id = "e1", routineId = "o1", executedAt = LocalDateTime.now(), totalTimeSpentSeconds = 60, totalStepsCount = 2)
      ),
      workflowRules = listOf(
        WorkflowRule(
          uuId = "wr1",
          title = "Archive after 30 days",
          scope = WorkflowScope.Global,
          trigger = WorkflowTrigger.ReminderAgeExceeded(days = 30),
          action = WorkflowAction.ArchiveReminder
        )
      ),
      workflowTemplates = listOf(
        WorkflowTemplate(
          id = "wt1",
          title = "Archive template",
          trigger = WorkflowTrigger.ReminderAgeExceeded(days = 30),
          action = WorkflowAction.ArchiveReminder
        )
      )
    )

    val summary = useCase(envelope)

    assertEquals(1, summary.routinesImported)
    assertEquals(1, summary.routineExecutionsImported)
    assertEquals(1, summary.workflowRulesImported)
    assertEquals(1, summary.workflowTemplatesImported)
    coVerify { routineRepository.save(match { it.id == "o1" }) }
    coVerify { routineExecutionRepository.save(match { it.id == "e1" }) }
    coVerify { workflowRuleRepository.save(match { it.uuId == "wr1" }) }
    coVerify { workflowTemplateRepository.save(match { it.id == "wt1" }) }
  }

  @Test
  fun `activates imported reminders that are active and not removed`() = runTest {
    val envelope = BackupEnvelope(
      reminders = listOf(
        ReminderV2(
          uuId = "r1",
          summary = "Take pills",
          isActive = true,
          isRemoved = false,
          schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0))
        )
      )
    )

    useCase(envelope)

    coVerify { activateReminderUseCase(match { it.uuId == "r1" }) }
  }

  @Test
  fun `does not activate imported reminders that are inactive or removed`() = runTest {
    val envelope = BackupEnvelope(
      reminders = listOf(
        ReminderV2(
          uuId = "inactive",
          summary = "Completed",
          isActive = false,
          isRemoved = false,
          schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0))
        ),
        ReminderV2(
          uuId = "removed",
          summary = "Deleted",
          isActive = true,
          isRemoved = true,
          schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0))
        )
      )
    )

    useCase(envelope)

    coVerify(exactly = 0) { activateReminderUseCase(any()) }
  }

  @Test
  fun `recalculates occurrence caches for every imported birthday`() = runTest {
    val envelope = BackupEnvelope(
      birthdays = listOf(
        Birthday(uuId = "b1", name = "Alex", syncState = SyncState.Synced),
        Birthday(uuId = "b2", name = "Sam", syncState = SyncState.Synced)
      )
    )

    useCase(envelope)

    coVerify { calculateBirthdayOccurrencesUseCase("b1") }
    coVerify { calculateBirthdayOccurrencesUseCase("b2") }
  }
}

package com.github.naz013.feature.workflow

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.logic.workflow.ApplyWorkflowTemplateUseCase
import com.github.naz013.logic.workflow.SaveWorkflowTemplateUseCase
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val ARCHIVE_TEMPLATE_ID = "built_in_template_archive_completed_reminders"

class WorkflowRulesUtilTest {

  private val workflowTemplateRepository = mockk<WorkflowTemplateRepository>()
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>()
  private val applyWorkflowTemplateUseCase = mockk<ApplyWorkflowTemplateUseCase>(relaxed = true)
  private val saveWorkflowTemplateUseCase = mockk<SaveWorkflowTemplateUseCase>(relaxed = true)

  private lateinit var util: WorkflowRulesUtil

  @Before
  fun setUp() {
    util = WorkflowRulesUtil(
      workflowTemplateRepository,
      workflowRuleRepository,
      applyWorkflowTemplateUseCase,
      saveWorkflowTemplateUseCase
    )
  }

  @Test
  fun `seeds exactly three built-in templates when the template table is empty`() = runTest {
    coEvery { workflowTemplateRepository.countAll() } returns 0
    coEvery { workflowRuleRepository.countAll() } returns 1

    util.initDefaultIfEmpty()

    coVerify(exactly = 3) { saveWorkflowTemplateUseCase(any()) }
  }

  @Test
  fun `does not seed templates when the template table already has entries`() = runTest {
    coEvery { workflowTemplateRepository.countAll() } returns 3
    coEvery { workflowRuleRepository.countAll() } returns 1

    util.initDefaultIfEmpty()

    coVerify(exactly = 0) { saveWorkflowTemplateUseCase(any()) }
  }

  @Test
  fun `applies the archive template globally when the rule table is empty`() = runTest {
    val archiveTemplate = WorkflowTemplate(
      id = ARCHIVE_TEMPLATE_ID,
      title = "Archive completed reminders after 30 days",
      category = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
      trigger = WorkflowTrigger.ReminderAgeExceeded(days = 30),
      action = WorkflowAction.ArchiveReminder
    )
    coEvery { workflowTemplateRepository.countAll() } returns 3
    coEvery { workflowTemplateRepository.getAll() } returns listOf(archiveTemplate)
    coEvery { workflowRuleRepository.countAll() } returns 0

    util.initDefaultIfEmpty()

    coVerify(exactly = 1) { applyWorkflowTemplateUseCase(archiveTemplate, WorkflowScope.Global) }
  }

  @Test
  fun `does not apply the archive template when the rule table already has entries`() = runTest {
    coEvery { workflowTemplateRepository.countAll() } returns 3
    coEvery { workflowRuleRepository.countAll() } returns 1

    util.initDefaultIfEmpty()

    coVerify(exactly = 0) { applyWorkflowTemplateUseCase(any(), any()) }
    coVerify(exactly = 0) { workflowTemplateRepository.getAll() }
  }

  @Test
  fun `does not apply anything when the archive template hasn't been seeded yet`() = runTest {
    coEvery { workflowTemplateRepository.countAll() } returns 3
    coEvery { workflowTemplateRepository.getAll() } returns emptyList()
    coEvery { workflowRuleRepository.countAll() } returns 0

    util.initDefaultIfEmpty()

    coVerify(exactly = 0) { applyWorkflowTemplateUseCase(any(), any()) }
  }

  @Test
  fun `is a no-op on a subsequent call once both tables are populated`() = runTest {
    coEvery { workflowTemplateRepository.countAll() } returns 3
    coEvery { workflowRuleRepository.countAll() } returns 1

    util.initDefaultIfEmpty()
    util.initDefaultIfEmpty()

    coVerify(exactly = 0) { saveWorkflowTemplateUseCase(any()) }
    coVerify(exactly = 0) { applyWorkflowTemplateUseCase(any(), any()) }
  }

  @Test
  fun `seeds the escalate-on-snooze template with its trigger and action`() = runTest {
    coEvery { workflowTemplateRepository.countAll() } returns 0
    coEvery { workflowRuleRepository.countAll() } returns 1
    val saved = mutableListOf<WorkflowTemplate>()
    coEvery { saveWorkflowTemplateUseCase(any()) } answers {
      saved.add(firstArg())
      firstArg()
    }

    util.initDefaultIfEmpty()

    val escalate = saved.first { it.trigger is WorkflowTrigger.ReminderSnoozedNTimes }
    assertEquals(3, (escalate.trigger as WorkflowTrigger.ReminderSnoozedNTimes).count)
    assertEquals(WorkflowAction.ApplyNotificationOverride::class, escalate.action::class)
  }
}

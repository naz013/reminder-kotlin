package com.github.naz013.sync.local

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.WorkflowTemplateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkflowTemplateRepositoryCallerTest {
  private val workflowTemplateRepository = mockk<WorkflowTemplateRepository>()
  private val caller = WorkflowTemplateRepositoryCaller(workflowTemplateRepository)

  private fun template(id: String, syncState: SyncState = SyncState.WaitingForUpload) = WorkflowTemplate(
    id = id,
    trigger = WorkflowTrigger.ReminderCompleted,
    action = WorkflowAction.ArchiveReminder,
    syncState = syncState
  )

  @Test
  fun `getById delegates to the repository`() = runTest {
    coEvery { workflowTemplateRepository.getById("1") } returns template("1")

    val result = caller.getById("1")

    assertEquals("1", result?.id)
  }

  @Test
  fun `getIdsByState delegates to the repository`() = runTest {
    coEvery { workflowTemplateRepository.getIdsByState(listOf(SyncState.WaitingForUpload)) } returns listOf("1")

    val result = caller.getIdsByState(listOf(SyncState.WaitingForUpload))

    assertEquals(listOf("1"), result)
  }

  @Test
  fun `updateSyncState delegates to the repository`() = runTest {
    coEvery { workflowTemplateRepository.updateSyncState("1", SyncState.Synced) } returns Unit

    caller.updateSyncState("1", SyncState.Synced)

    coVerify { workflowTemplateRepository.updateSyncState("1", SyncState.Synced) }
  }

  @Test
  fun `insertOrUpdate saves the template as Synced`() = runTest {
    coEvery { workflowTemplateRepository.save(any()) } returns Unit

    caller.insertOrUpdate(template("1"))

    coVerify { workflowTemplateRepository.save(match { it.id == "1" && it.syncState == SyncState.Synced }) }
  }

  @Test(expected = IllegalArgumentException::class)
  fun `insertOrUpdate rejects an item that is not a WorkflowTemplate`() = runTest {
    caller.insertOrUpdate("not a template")
  }

  @Test
  fun `getAllIds delegates to the repository`() = runTest {
    coEvery { workflowTemplateRepository.getAllIds() } returns listOf("1", "2")

    val result = caller.getAllIds()

    assertEquals(listOf("1", "2"), result)
  }
}

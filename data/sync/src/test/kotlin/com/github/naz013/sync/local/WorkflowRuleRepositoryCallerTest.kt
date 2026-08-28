package com.github.naz013.sync.local

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.WorkflowRuleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkflowRuleRepositoryCallerTest {
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>()
  private val caller = WorkflowRuleRepositoryCaller(workflowRuleRepository)

  private fun rule(id: String, syncState: SyncState = SyncState.WaitingForUpload) = WorkflowRule(
    uuId = id,
    trigger = WorkflowTrigger.ReminderCompleted,
    action = WorkflowAction.ArchiveReminder,
    syncState = syncState
  )

  @Test
  fun `getById delegates to the repository`() = runTest {
    coEvery { workflowRuleRepository.getById("1") } returns rule("1")

    val result = caller.getById("1")

    assertEquals("1", result?.uuId)
  }

  @Test
  fun `getIdsByState delegates to the repository`() = runTest {
    coEvery { workflowRuleRepository.getIdsByState(listOf(SyncState.WaitingForUpload)) } returns listOf("1")

    val result = caller.getIdsByState(listOf(SyncState.WaitingForUpload))

    assertEquals(listOf("1"), result)
  }

  @Test
  fun `updateSyncState delegates to the repository`() = runTest {
    coEvery { workflowRuleRepository.updateSyncState("1", SyncState.Synced) } returns Unit

    caller.updateSyncState("1", SyncState.Synced)

    coVerify { workflowRuleRepository.updateSyncState("1", SyncState.Synced) }
  }

  @Test
  fun `insertOrUpdate saves the rule as Synced`() = runTest {
    coEvery { workflowRuleRepository.save(any()) } returns Unit

    caller.insertOrUpdate(rule("1"))

    coVerify { workflowRuleRepository.save(match { it.uuId == "1" && it.syncState == SyncState.Synced }) }
  }

  @Test(expected = IllegalArgumentException::class)
  fun `insertOrUpdate rejects an item that is not a WorkflowRule`() = runTest {
    caller.insertOrUpdate("not a rule")
  }

  @Test
  fun `getAllIds delegates to the repository`() = runTest {
    coEvery { workflowRuleRepository.getAllIds() } returns listOf("1", "2")

    val result = caller.getAllIds()

    assertEquals(listOf("1", "2"), result)
  }
}

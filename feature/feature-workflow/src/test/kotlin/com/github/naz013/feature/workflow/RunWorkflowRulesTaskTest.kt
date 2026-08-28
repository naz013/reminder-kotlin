package com.github.naz013.feature.workflow

import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RunWorkflowRulesTaskTest {

  private val workflowTriggerRunner = mockk<WorkflowTriggerRunner>(relaxed = true)
  private val task = RunWorkflowRulesTask(workflowTriggerRunner)

  @Test
  fun `run runs the daily polling rules and succeeds`() = runTest {
    coEvery { workflowTriggerRunner.runDailyPolling() } returns Unit

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 1) { workflowTriggerRunner.runDailyPolling() }
  }
}

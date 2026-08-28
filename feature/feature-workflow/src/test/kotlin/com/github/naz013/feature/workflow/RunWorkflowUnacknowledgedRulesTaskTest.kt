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

class RunWorkflowUnacknowledgedRulesTaskTest {

  private val workflowTriggerRunner = mockk<WorkflowTriggerRunner>(relaxed = true)
  private val task = RunWorkflowUnacknowledgedRulesTask(workflowTriggerRunner)

  @Test
  fun `run runs the unacknowledged polling rules and succeeds`() = runTest {
    coEvery { workflowTriggerRunner.runUnacknowledgedPolling() } returns Unit

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 1) { workflowTriggerRunner.runUnacknowledgedPolling() }
  }
}

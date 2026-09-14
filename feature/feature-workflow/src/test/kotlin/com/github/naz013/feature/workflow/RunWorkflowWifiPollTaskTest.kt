package com.github.naz013.feature.workflow

import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RunWorkflowWifiPollTaskTest {

  private val currentWifiSsidReader = mockk<CurrentWifiSsidReader>()
  private val workflowWifiPollState = mockk<WorkflowWifiPollState>(relaxed = true)
  private val workflowTriggerRunner = mockk<WorkflowTriggerRunner>(relaxed = true)

  private val task = RunWorkflowWifiPollTask(currentWifiSsidReader, workflowWifiPollState, workflowTriggerRunner)

  @Before
  fun setUp() {
    every { workflowWifiPollState.lastKnownSsid } returns null
  }

  @Test
  fun `fires connected and remembers the SSID when a new network appears`() = runTest {
    every { currentWifiSsidReader.read() } returns "Home WiFi"

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 1) { workflowTriggerRunner.onWifiConnected("Home WiFi") }
    coVerify(exactly = 0) { workflowTriggerRunner.onWifiDisconnected(any()) }
    verify(exactly = 1) { workflowWifiPollState.lastKnownSsid = "Home WiFi" }
  }

  @Test
  fun `fires disconnected and clears the remembered SSID when the network disappears`() = runTest {
    every { workflowWifiPollState.lastKnownSsid } returns "Home WiFi"
    every { currentWifiSsidReader.read() } returns null

    task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    coVerify(exactly = 1) { workflowTriggerRunner.onWifiDisconnected("Home WiFi") }
    coVerify(exactly = 0) { workflowTriggerRunner.onWifiConnected(any()) }
    verify(exactly = 1) { workflowWifiPollState.lastKnownSsid = null }
  }

  @Test
  fun `fires disconnected for the old network then connected for the new one when the SSID changed`() = runTest {
    every { workflowWifiPollState.lastKnownSsid } returns "Home WiFi"
    every { currentWifiSsidReader.read() } returns "Office WiFi"

    task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    coVerify(exactly = 1) { workflowTriggerRunner.onWifiDisconnected("Home WiFi") }
    coVerify(exactly = 1) { workflowTriggerRunner.onWifiConnected("Office WiFi") }
    verify(exactly = 1) { workflowWifiPollState.lastKnownSsid = "Office WiFi" }
  }

  @Test
  fun `does nothing when the SSID has not changed since the last poll`() = runTest {
    every { workflowWifiPollState.lastKnownSsid } returns "Home WiFi"
    every { currentWifiSsidReader.read() } returns "Home WiFi"

    task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    coVerify(exactly = 0) { workflowTriggerRunner.onWifiConnected(any()) }
    coVerify(exactly = 0) { workflowTriggerRunner.onWifiDisconnected(any()) }
    verify(exactly = 0) { workflowWifiPollState.lastKnownSsid = any() }
  }

  @Test
  fun `does nothing when there is no network before or after`() = runTest {
    every { currentWifiSsidReader.read() } returns null

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 0) { workflowTriggerRunner.onWifiConnected(any()) }
    coVerify(exactly = 0) { workflowTriggerRunner.onWifiDisconnected(any()) }
  }
}

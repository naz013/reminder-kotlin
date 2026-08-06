package com.github.naz013.work

import androidx.work.NetworkType
import androidx.work.WorkInfo
import com.github.naz013.workapi.ExistingWorkPolicy
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDataMapperTest {

  @Test
  fun `toWorkData carries the task key plus every supported value type`() {
    val taskData = TaskData.builder()
      .putString("name", "task")
      .putBoolean("enabled", true)
      .putStringArray("tags", arrayOf("a", "b"))
      .build()

    val workData = taskData.toWorkData("my-task-key")

    assertEquals("my-task-key", workData.getString(GenericTaskWorker.KEY_TASK_KEY))
    assertEquals("task", workData.getString("name"))
    assertTrue(workData.getBoolean("enabled", false))
    assertEquals(listOf("a", "b"), workData.getStringArray("tags")?.toList())
  }

  @Test
  fun `taskKeyOnlyWorkData carries only the task key`() {
    val workData = taskKeyOnlyWorkData("my-task-key")

    assertEquals("my-task-key", workData.getString(GenericTaskWorker.KEY_TASK_KEY))
    assertEquals(1, workData.keyValueMap.size)
  }

  @Test
  fun `TaskData round trips through Data unchanged`() {
    val original = TaskData.builder()
      .putString("name", "task")
      .putBoolean("enabled", true)
      .build()

    val roundTripped = original.toWorkData("key").toTaskData()

    assertEquals("task", roundTripped.getString("name"))
    assertTrue(roundTripped.getBoolean("enabled"))
  }

  @Test
  fun `toNetworkType maps every requirement`() {
    assertEquals(NetworkType.NOT_REQUIRED, NetworkRequirement.NONE.toNetworkType())
    assertEquals(NetworkType.CONNECTED, NetworkRequirement.CONNECTED.toNetworkType())
    assertEquals(NetworkType.UNMETERED, NetworkRequirement.UNMETERED.toNetworkType())
    assertEquals(NetworkType.METERED, NetworkRequirement.METERED.toNetworkType())
  }

  @Test
  fun `toConstraints carries network and battery requirements`() {
    val request = WorkRequest(
      taskKey = "key",
      networkRequirement = NetworkRequirement.CONNECTED,
      requiresBatteryNotLow = true
    )

    val constraints = request.toConstraints()

    assertEquals(NetworkType.CONNECTED, constraints.requiredNetworkType)
    assertTrue(constraints.requiresBatteryNotLow())
  }

  @Test
  fun `toAndroidPolicy maps both policies`() {
    assertEquals(androidx.work.ExistingWorkPolicy.KEEP, ExistingWorkPolicy.KEEP.toAndroidPolicy())
    assertEquals(androidx.work.ExistingWorkPolicy.REPLACE, ExistingWorkPolicy.REPLACE.toAndroidPolicy())
  }

  @Test
  fun `toWorkState maps every terminal and enqueued state`() {
    assertEquals(WorkState.Enqueued, stateOf(WorkInfo.State.ENQUEUED).toWorkState())
    assertEquals(WorkState.Succeeded, stateOf(WorkInfo.State.SUCCEEDED).toWorkState())
    assertEquals(WorkState.Failed, stateOf(WorkInfo.State.FAILED).toWorkState())
    assertEquals(WorkState.Cancelled, stateOf(WorkInfo.State.CANCELLED).toWorkState())
    assertEquals(WorkState.Blocked, stateOf(WorkInfo.State.BLOCKED).toWorkState())
  }

  @Test
  fun `toWorkState maps RUNNING to Running carrying the progress data`() {
    val progress = TaskData.builder().putString("step", "1").build().toWorkData("key")
    val workInfo = mockk<WorkInfo>()
    every { workInfo.state } returns WorkInfo.State.RUNNING
    every { workInfo.progress } returns progress

    val result = workInfo.toWorkState()

    assertTrue(result is WorkState.Running)
    assertEquals("1", (result as WorkState.Running).progress.getString("step"))
  }

  private fun stateOf(state: WorkInfo.State): WorkInfo {
    val workInfo = mockk<WorkInfo>()
    every { workInfo.state } returns state
    return workInfo
  }
}

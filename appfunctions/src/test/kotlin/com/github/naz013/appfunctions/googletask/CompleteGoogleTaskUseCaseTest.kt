package com.github.naz013.appfunctions.googletask

import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CompleteGoogleTaskUseCaseTest {

  private val googleTasksApi = mockk<GoogleTasksApi>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>(relaxUnitFun = true)
  private val useCase = CompleteGoogleTaskUseCase(googleTasksApi, googleTaskRepository)

  @Test
  fun `invoke marks the task complete through the api and saves the result locally`() = runTest {
    val existing = GoogleTask(taskId = "task-1", listId = "list-1", title = "Buy milk")
    val completed = existing.copy(status = GoogleTask.TASKS_COMPLETE, completeDate = 123L)
    coEvery { googleTaskRepository.getById("task-1") } returns existing
    coEvery { googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, existing) } returns completed

    val result = useCase("task-1")

    assertEquals(completed, result)
    coVerify { googleTaskRepository.save(completed) }
  }

  @Test
  fun `invoke returns null and never calls the api when no local task exists`() = runTest {
    coEvery { googleTaskRepository.getById("missing") } returns null

    val result = useCase("missing")

    assertNull(result)
    coVerify(exactly = 0) { googleTasksApi.updateTaskStatus(any(), any()) }
    coVerify(exactly = 0) { googleTaskRepository.save(any()) }
  }

  @Test
  fun `invoke returns null and does not save when the api update fails`() = runTest {
    val existing = GoogleTask(taskId = "task-1", listId = "list-1", title = "Buy milk")
    coEvery { googleTaskRepository.getById("task-1") } returns existing
    coEvery { googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, existing) } returns null

    val result = useCase("task-1")

    assertNull(result)
    coVerify(exactly = 0) { googleTaskRepository.save(any()) }
  }
}

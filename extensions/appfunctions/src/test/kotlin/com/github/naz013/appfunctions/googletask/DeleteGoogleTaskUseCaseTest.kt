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

class DeleteGoogleTaskUseCaseTest {

  private val googleTasksApi = mockk<GoogleTasksApi>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>(relaxUnitFun = true)
  private val useCase = DeleteGoogleTaskUseCase(googleTasksApi, googleTaskRepository)

  @Test
  fun `invoke deletes through the api and locally when it succeeds`() = runTest {
    val existing = GoogleTask(taskId = "task-1", listId = "list-1", title = "Buy milk")
    coEvery { googleTaskRepository.getById("task-1") } returns existing
    coEvery { googleTasksApi.deleteTask(existing) } returns true

    val result = useCase("task-1")

    assertEquals(existing, result)
    coVerify { googleTaskRepository.delete("task-1") }
  }

  @Test
  fun `invoke returns null and never calls the api when no local task exists`() = runTest {
    coEvery { googleTaskRepository.getById("missing") } returns null

    val result = useCase("missing")

    assertNull(result)
    coVerify(exactly = 0) { googleTasksApi.deleteTask(any()) }
    coVerify(exactly = 0) { googleTaskRepository.delete(any()) }
  }

  @Test
  fun `invoke returns null and does not delete locally when the api delete fails`() = runTest {
    val existing = GoogleTask(taskId = "task-1", listId = "list-1", title = "Buy milk")
    coEvery { googleTaskRepository.getById("task-1") } returns existing
    coEvery { googleTasksApi.deleteTask(existing) } returns false

    val result = useCase("task-1")

    assertNull(result)
    coVerify(exactly = 0) { googleTaskRepository.delete(any()) }
  }
}

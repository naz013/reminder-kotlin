package com.github.naz013.appfunctions.googletask

import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime as JavaLocalDateTime
import org.threeten.bp.LocalDateTime as ThreeTenLocalDateTime

class UpdateGoogleTaskUseCaseTest {

  private val googleTasksApi = mockk<GoogleTasksApi>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>(relaxUnitFun = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val useCase = UpdateGoogleTaskUseCase(googleTasksApi, googleTaskRepository, dateTimeManager)

  @Test
  fun `invoke updates the task through the api and saves the result locally, preserving status`() = runTest {
    val existing =
      GoogleTask(taskId = "task-1", listId = "list-1", title = "Buy milk", status = GoogleTask.TASKS_COMPLETE)
    every {
      dateTimeManager.toMillis(ThreeTenLocalDateTime.of(2026, 8, 5, 10, 0))
    } returns 123L
    val expectedUpdate = existing.copy(title = "Buy oat milk", notes = "2%", dueDate = 123L)
    coEvery { googleTaskRepository.getById("task-1") } returns existing
    coEvery { googleTasksApi.updateTask(expectedUpdate) } returns expectedUpdate

    val result =
      useCase(
        id = "task-1",
        title = "Buy oat milk",
        notes = "2%",
        dueDateTime = JavaLocalDateTime.of(2026, 8, 5, 10, 0),
      )

    assertEquals("Buy oat milk", result?.title)
    assertEquals(GoogleTask.TASKS_COMPLETE, result?.status)
    coVerify { googleTaskRepository.save(expectedUpdate) }
  }

  @Test
  fun `invoke returns null and never calls the api when no local task exists`() = runTest {
    coEvery { googleTaskRepository.getById("missing") } returns null

    val result = useCase(id = "missing", title = "Title", notes = null, dueDateTime = null)

    assertNull(result)
    coVerify(exactly = 0) { googleTasksApi.updateTask(any()) }
    coVerify(exactly = 0) { googleTaskRepository.save(any()) }
  }

  @Test
  fun `invoke returns null and does not save when the api update fails`() = runTest {
    val existing = GoogleTask(taskId = "task-1", listId = "list-1", title = "Buy milk")
    coEvery { googleTaskRepository.getById("task-1") } returns existing
    coEvery { googleTasksApi.updateTask(any()) } returns null

    val result = useCase(id = "task-1", title = "Buy oat milk", notes = null, dueDateTime = null)

    assertNull(result)
    coVerify(exactly = 0) { googleTaskRepository.save(any()) }
  }
}

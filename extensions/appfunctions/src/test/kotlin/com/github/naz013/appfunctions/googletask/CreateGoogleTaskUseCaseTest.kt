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

class CreateGoogleTaskUseCaseTest {

  private val googleTasksApi = mockk<GoogleTasksApi>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>(relaxUnitFun = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val useCase = CreateGoogleTaskUseCase(googleTasksApi, googleTaskRepository, dateTimeManager)

  @Test
  fun `invoke pushes the task through the api and saves the server response locally`() = runTest {
    val dueDateTime = JavaLocalDateTime.of(2026, 8, 1, 9, 0)
    every { dateTimeManager.toMillis(ThreeTenLocalDateTime.of(2026, 8, 1, 9, 0)) } returns 1_754_038_800_000L
    val saved = GoogleTask(taskId = "server-1", title = "Buy milk")
    val expectedTask = GoogleTask(
      title = "Buy milk",
      notes = "2%",
      dueDate = 1_754_038_800_000L,
      status = GoogleTask.TASKS_NEED_ACTION
    )
    coEvery { googleTasksApi.saveTask(expectedTask) } returns saved

    val result = useCase(title = "Buy milk", notes = "2%", dueDateTime = dueDateTime)

    assertEquals(saved, result)
    coVerify { googleTaskRepository.save(saved) }
  }

  @Test
  fun `invoke leaves dueDate at zero when no due date is given`() = runTest {
    val saved = GoogleTask(taskId = "server-1", title = "Buy milk")
    coEvery {
      googleTasksApi.saveTask(
        GoogleTask(title = "Buy milk", notes = "", dueDate = 0L, status = GoogleTask.TASKS_NEED_ACTION)
      )
    } returns saved

    val result = useCase(title = "Buy milk", notes = null, dueDateTime = null)

    assertEquals(saved, result)
  }

  @Test
  fun `invoke returns null and does not save locally when the api call fails`() = runTest {
    coEvery { googleTasksApi.saveTask(any()) } returns null

    val result = useCase(title = "Buy milk", notes = null, dueDateTime = null)

    assertNull(result)
    coVerify(exactly = 0) { googleTaskRepository.save(any()) }
  }
}

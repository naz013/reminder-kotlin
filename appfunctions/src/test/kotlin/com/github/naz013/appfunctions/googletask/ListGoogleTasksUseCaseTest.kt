package com.github.naz013.appfunctions.googletask

import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ListGoogleTasksUseCaseTest {

  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val useCase = ListGoogleTasksUseCase(googleTaskRepository)

  private val active = GoogleTask(taskId = "active", title = "Buy milk", status = GoogleTask.TASKS_NEED_ACTION)
  private val done = GoogleTask(taskId = "done", title = "Pay rent", status = GoogleTask.TASKS_COMPLETE)

  @Test
  fun `invoke excludes completed tasks by default`() = runTest {
    coEvery { googleTaskRepository.getAll() } returns listOf(active, done)

    val result = useCase(includeCompleted = false)

    assertEquals(listOf(active), result)
  }

  @Test
  fun `invoke includes completed tasks when asked`() = runTest {
    coEvery { googleTaskRepository.getAll() } returns listOf(active, done)

    val result = useCase(includeCompleted = true)

    assertEquals(listOf(active, done), result)
  }
}

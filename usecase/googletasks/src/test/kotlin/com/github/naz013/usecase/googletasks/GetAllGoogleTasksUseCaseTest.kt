package com.github.naz013.usecase.googletasks

import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllGoogleTasksUseCaseTest {

  @Test
  fun `invoke returns every task from the repository`() = runTest {
    val repository = mockk<GoogleTaskRepository>()
    val tasks = listOf(GoogleTask(taskId = "1"), GoogleTask(taskId = "2"))
    coEvery { repository.getAll() } returns tasks
    val useCase = GetAllGoogleTasksUseCase(repository)

    val result = useCase()

    assertEquals(tasks, result)
  }
}

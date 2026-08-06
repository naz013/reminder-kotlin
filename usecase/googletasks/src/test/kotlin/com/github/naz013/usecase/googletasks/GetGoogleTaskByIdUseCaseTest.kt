package com.github.naz013.usecase.googletasks

import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetGoogleTaskByIdUseCaseTest {

  @Test
  fun `invoke returns the task for the given id`() = runTest {
    val repository = mockk<GoogleTaskRepository>()
    val task = GoogleTask(taskId = "1")
    coEvery { repository.getById("1") } returns task
    val useCase = GetGoogleTaskByIdUseCase(repository)

    val result = useCase("1")

    assertEquals(task, result)
  }

  @Test
  fun `invoke returns null when the repository has no match`() = runTest {
    val repository = mockk<GoogleTaskRepository>()
    coEvery { repository.getById("missing") } returns null
    val useCase = GetGoogleTaskByIdUseCase(repository)

    val result = useCase("missing")

    assertNull(result)
  }
}

package com.github.naz013.appfunctions.googletask

import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchGoogleTasksUseCaseTest {

  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val useCase = SearchGoogleTasksUseCase(googleTaskRepository)

  @Test
  fun `invoke returns matches from the repository`() = runTest {
    val matches = listOf(GoogleTask(taskId = "task-1", listId = "list-1", title = "Buy milk"))
    coEvery { googleTaskRepository.search("milk") } returns matches

    val result = useCase("milk")

    assertEquals(matches, result)
  }
}

package com.github.naz013.usecase.googletasks

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskListRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllGoogleTaskListsUseCaseTest {

  @Test
  fun `invoke returns every task list from the repository`() = runTest {
    val repository = mockk<GoogleTaskListRepository>()
    val lists = listOf(GoogleTaskList(listId = "1"), GoogleTaskList(listId = "2"))
    coEvery { repository.getAll() } returns lists
    val useCase = GetAllGoogleTaskListsUseCase(repository)

    val result = useCase()

    assertEquals(lists, result)
  }
}

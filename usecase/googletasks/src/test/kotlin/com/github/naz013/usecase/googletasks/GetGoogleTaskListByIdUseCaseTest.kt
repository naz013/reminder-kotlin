package com.github.naz013.usecase.googletasks

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskListRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetGoogleTaskListByIdUseCaseTest {

  @Test
  fun `invoke returns the task list for the given id`() = runTest {
    val repository = mockk<GoogleTaskListRepository>()
    val list = GoogleTaskList(listId = "1")
    coEvery { repository.getById("1") } returns list
    val useCase = GetGoogleTaskListByIdUseCase(repository)

    val result = useCase("1")

    assertEquals(list, result)
  }

  @Test
  fun `invoke returns null when the repository has no match`() = runTest {
    val repository = mockk<GoogleTaskListRepository>()
    coEvery { repository.getById("missing") } returns null
    val useCase = GetGoogleTaskListByIdUseCase(repository)

    val result = useCase("missing")

    assertNull(result)
  }
}

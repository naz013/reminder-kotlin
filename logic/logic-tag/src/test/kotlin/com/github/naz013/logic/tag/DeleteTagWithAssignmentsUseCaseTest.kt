package com.github.naz013.logic.tag

import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteTagWithAssignmentsUseCaseTest {
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private lateinit var useCase: DeleteTagWithAssignmentsUseCase

  @Before
  fun setUp() {
    coEvery { tagAssignmentRepository.detachAllForTag(any()) } returns Unit
    coEvery { tagRepository.delete(any()) } returns Unit
    useCase = DeleteTagWithAssignmentsUseCase(tagRepository, tagAssignmentRepository)
  }

  @Test
  fun `detaches all assignments before deleting the tag`() =
    runTest {
      useCase("7")

      coVerifyOrder {
        tagAssignmentRepository.detachAllForTag("7")
        tagRepository.delete("7")
      }
    }
}
